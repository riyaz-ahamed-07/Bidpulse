package com.bidpulse.service;

import com.bidpulse.dto.auction.AuctionDto;
import com.bidpulse.dto.auction.CreateAuctionRequest;
import com.bidpulse.dto.bid.BidDto;
import com.bidpulse.model.*;
import com.bidpulse.repository.AuctionRepository;
import com.bidpulse.repository.BidRepository;
import com.bidpulse.repository.UserRepository;
import com.bidpulse.websocket.WsEventPayload;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final UserRepository userRepository;

    // external services — implement later
    private final WalletService walletService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messaging;

    /* ------------------ Auction CRUD ------------------ */

    public AuctionDto createAuction(CreateAuctionRequest req, Long sellerId) {
        var seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Seller not found"));

        var auction = Auction.builder()
            .title(req.getTitle())
            .description(req.getDescription())
            .imageData(req.getImageData())
            .seller(seller)
            .startingPrice(req.getStartingPrice())
            .minIncrement(req.getMinIncrement())
            .reservePrice(req.getReservePrice())
            .startTime(req.getStartTime())
            .endTime(req.getEndTime())
            .status(AuctionStatus.DRAFT)
            .build();

        auction = auctionRepository.save(auction);
        return AuctionDto.from(auction);
    }

    public Page<AuctionDto> listAuctions(Pageable pageable) {
        return auctionRepository.findAll(pageable).map(AuctionDto::from);
    }

    public AuctionDto getAuction(Long auctionId) {
        var a = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Auction not found"));
        // do NOT fetch bids here; client can call /bids endpoint
        return AuctionDto.from(a);
    }

    @Transactional
    public AuctionDto updateAuction(Long auctionId, CreateAuctionRequest req, Long actorId) {
        var auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Auction not found"));

        if (auction.getStatus() != AuctionStatus.DRAFT && auction.getStatus() != AuctionStatus.RUNNING) {
            throw new ResponseStatusException(BAD_REQUEST, "Auction cannot be updated in current status");
        }
        if (!auction.getSeller().getId().equals(actorId)) {
            throw new ResponseStatusException(FORBIDDEN, "Only seller can update auction");
        }

        auction.setTitle(req.getTitle());
        auction.setDescription(req.getDescription());
        auction.setStartingPrice(req.getStartingPrice());
        auction.setMinIncrement(req.getMinIncrement());
        auction.setReservePrice(req.getReservePrice());
        auction.setStartTime(req.getStartTime());
        auction.setEndTime(req.getEndTime());

        auction = auctionRepository.save(auction);
        return AuctionDto.from(auction);
    }

    /* ------------------ Start / End actions ------------------ */

    @Transactional
    public AuctionDto startAuction(Long auctionId, Long actorId, boolean forceByAdmin) {
        var auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Auction not found"));

        boolean allowed = forceByAdmin || auction.getSeller().getId().equals(actorId);
        if (!allowed) throw new ResponseStatusException(FORBIDDEN, "Not allowed to start auction");

        if (auction.getStatus() != AuctionStatus.DRAFT) {
            throw new ResponseStatusException(BAD_REQUEST, "Only DRAFT auctions can be started");
        }

        auction.setStatus(AuctionStatus.RUNNING);
        if (auction.getStartTime() == null) auction.setStartTime(Instant.now());
        auctionRepository.save(auction);

        // broadcast start event minimal payload
        messaging.convertAndSend("/topic/auction." + auction.getId() + ".events",
            new WsEventPayload("auction_started", auction.getId()));

        return AuctionDto.from(auction);
    }

    @Transactional
    public AuctionDto endAuction(Long auctionId, Long actorId, boolean forceByAdmin) {
        // Use pessimistic locking when finalizing in scheduler or admin
        var auction = auctionRepository.findByIdForUpdate(auctionId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Auction not found"));

        boolean allowed = forceByAdmin || auction.getSeller().getId().equals(actorId);
        if (!allowed) throw new ResponseStatusException(FORBIDDEN, "Not allowed to end auction");

        if (auction.getStatus() != AuctionStatus.RUNNING) {
            throw new ResponseStatusException(BAD_REQUEST, "Only RUNNING auctions can be ended");
        }

        // find winner (top bid)
        Optional.ofNullable(bidRepository.findTopByAuctionIdOrderByAmountDesc(auction.getId())
                .orElse(null))
            .ifPresentOrElse(winner -> {
                // charge winner — ideally idempotent and using PaymentTransaction
                walletService.chargeReserved(winner.getBidder().getId(), winner.getAmount(), auction.getId());
                notificationService.notify(winner.getBidder().getId(),
                        NotificationType.WON,
                        "You won auction " + auction.getTitle());
                // publish event
                messaging.convertAndSend("/topic/auction." + auction.getId() + ".events",
                    new WsEventPayload("auction_ended", auction.getId(), winner.getId(), winner.getAmount()));
            }, () -> {
                // no bids -> no winner
                messaging.convertAndSend("/topic/auction." + auction.getId() + ".events",
                    new WsEventPayload("auction_ended_no_winner", auction.getId()));
            });

        auction.setStatus(AuctionStatus.ENDED);
        auctionRepository.save(auction);
        return AuctionDto.from(auction);
    }

    /* ------------------ Bidding (central place) ------------------ */

    /**
     * Place a bid. This method executes inside a transaction and obtains a DB-level lock
     * on the auction row to prevent double-wins.
     */
    @Transactional
    public BidDto placeBid(Long auctionId, Long bidderId, BigDecimal amount) {
        // lock the auction row
        var auction = auctionRepository.findByIdForUpdate(auctionId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Auction not found"));

        // validations
        if (auction.getStatus() != AuctionStatus.RUNNING) {
            throw new ResponseStatusException(BAD_REQUEST, "Auction not running");
        }
        var now = Instant.now();
        if (auction.getStartTime() != null && now.isBefore(auction.getStartTime())) {
            throw new ResponseStatusException(BAD_REQUEST, "Auction has not started yet");
        }
        if (auction.getEndTime() != null && now.isAfter(auction.getEndTime())) {
            throw new ResponseStatusException(BAD_REQUEST, "Auction already ended");
        }
        if (auction.getSeller().getId().equals(bidderId)) {
            throw new ResponseStatusException(FORBIDDEN, "Seller cannot bid on own auction");
        }

        BigDecimal currentHighest = auction.getHighestBidAmount() == null ? auction.getStartingPrice() : auction.getHighestBidAmount();
        BigDecimal required = currentHighest.add(auction.getMinIncrement());
        if (amount.compareTo(required) < 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Bid amount too low. Minimum required: " + required);
        }

        // reserve funds in wallet (throws ResponseStatusException if insufficient)
        walletService.reserve(bidderId, amount, auction.getId());

        // persist bid
        var bidder = userRepository.findById(bidderId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Bidder not found"));

        var bid = Bid.builder()
                .auction(auction)
                .bidder(bidder)
                .amount(amount)
                .status(BidStatus.PLACED)
                .build();
        bid = bidRepository.save(bid);

        // update auction denormalized fields
        auction.setHighestBidAmount(amount);
        auction.setHighestBidderId(bidder.getId());
        auctionRepository.save(auction); // inside same transaction/lock

        // notify previous highest bidder and create outbid notification
        // (simplified: we query top2 and notify previous if present)
        var topBids = bidRepository.findTop10ByAuctionIdOrderByAmountDesc(auction.getId());
        if (topBids.size() > 1) {
            var previousWinner = topBids.get(1);
            walletService.release(previousWinner.getBidder().getId(), previousWinner.getAmount(), auction.getId());
            notificationService.notify(previousWinner.getBidder().getId(),
                    NotificationType.OUTBID,
                    "You were outbid on " + auction.getTitle());
        }

        // broadcast minimal WS event
        messaging.convertAndSend("/topic/auction." + auction.getId(),
            new WsEventPayload("new_bid", auction.getId(), bid.getId(), amount, bidder.getId(), bid.getPlacedAt()));

        return BidDto.from(bid);
    }
}
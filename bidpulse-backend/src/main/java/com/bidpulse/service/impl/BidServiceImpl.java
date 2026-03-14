package com.bidpulse.service.impl;

import com.bidpulse.dto.bid.BidDto;
import com.bidpulse.dto.bid.PlaceBidRequest;
import com.bidpulse.model.*;
import com.bidpulse.repository.*;
import com.bidpulse.service.BidService;
import com.bidpulse.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.bidpulse.websocket.WsEventPayload;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final WalletRepository walletRepository;
    private final NotificationService notificationService;

    private final SimpMessagingTemplate messaging;

    @Override
    @Transactional
    public BidDto placeBid(Long auctionId, Long bidderUserId, PlaceBidRequest req) {
        // Fetch auction
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("auction not found"));

        // FIX 1: Use Enum comparison instead of String.equalsIgnoreCase
        if (auction.getStatus() != AuctionStatus.RUNNING) {
            throw new IllegalStateException("auction not running");
        }

        Instant now = Instant.now();
        if (auction.getStartTime() != null && now.isBefore(auction.getStartTime())) {
            throw new IllegalStateException("auction not started");
        }
        if (auction.getEndTime() != null && now.isAfter(auction.getEndTime())) {
            throw new IllegalStateException("auction ended");
        }

        BigDecimal amount = req.getAmount();
        BigDecimal minRequired = auction.getStartingPrice();
        
        // Capture previous highest bidder BEFORE updating the auction
        Long previousBidderId = auction.getHighestBidderId();

        if (auction.getHighestBidAmount() != null) {
            minRequired = auction.getHighestBidAmount().add(auction.getMinIncrement());
        }
        if (amount.compareTo(minRequired) < 0) {
            throw new IllegalArgumentException("bid too low, min required: " + minRequired);
        }

        // FIX 2: Ensure findByUserId exists in WalletRepository (see step 2 below)
        walletRepository.findByUserId(bidderUserId).ifPresent(w -> {
            if (w.getBalance().compareTo(amount) < 0) {
                throw new IllegalStateException("insufficient funds");
            }
            // Simple reservation logic
            w.setBalance(w.getBalance().subtract(amount));
            w.setReservedAmount(w.getReservedAmount().add(amount));
            walletRepository.save(w);
        });

        // FIX 3: Use BidStatus Enum instead of String "PLACED"
        // Also ensure user builder is correct
        User bidder = new User();
        bidder.setId(bidderUserId);

        Bid b = Bid.builder()
                .auction(auction)
                .bidder(bidder)
                .amount(amount)
                .placedAt(Instant.now())
                .status(BidStatus.ACCEPTED) // Changed to Enum
                .build();
        b = bidRepository.save(b);

        // Update Auction
        auction.setHighestBidAmount(amount);
        auction.setHighestBidderId(bidderUserId);
        auctionRepository.save(auction);

        // FIX 4: Call notifyOutbid with the previous bidder ID (if it exists)
        if (previousBidderId != null) {
            notificationService.notifyOutbid(previousBidderId, auctionId, b.getId(), "You were outbid");
        }
        // Broadcast the new bid to anyone listening on the WebSocket
        WsEventPayload payload = WsEventPayload.builder()
                .type("new_bid")
                .auctionId(auction.getId())
                .bidId(b.getId())
                .amount(amount)
                .bidderId(bidderUserId)
                .placedAt(b.getPlacedAt())
                .build();
        
        messaging.convertAndSend("/topic/auction." + auction.getId(), payload);

        return BidDto.from(b);
    }
}
package com.bidpulse.scheduler;

import com.bidpulse.model.Auction;
import com.bidpulse.model.AuctionStatus; 
import com.bidpulse.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class AuctionFinalizerScheduler {

    private final AuctionRepository auctionRepository;

    // YOUR EXISTING METHOD (The Grim Reaper)
    @Scheduled(fixedDelayString = "${auction.finalizer.interval-ms:5000}")
    public void finalizeExpired() {
        var now = Instant.now();
        List<Auction> list = auctionRepository.findByStatusAndEndTimeBefore(AuctionStatus.RUNNING, now);

        for (Auction a : list) {
            a.setStatus(AuctionStatus.ENDED);
            auctionRepository.save(a);
            log.info("Finalized auction id={}", a.getId());
        }
    }

    // THE NEW METHOD (The Alarm Clock)
    @Scheduled(fixedDelayString = "5000") // Runs every 5 seconds
    public void startScheduled() {
        var now = Instant.now();
        
        // Find auctions that are SCHEDULED, but their start time has arrived or passed
        List<Auction> list = auctionRepository.findByStatusAndStartTimeBefore(AuctionStatus.DRAFT, now);

        for (Auction a : list) {
            a.setStatus(AuctionStatus.RUNNING);
            auctionRepository.save(a);
            log.info("🚀 Auto-started auction id={}", a.getId());
        }
    }
}
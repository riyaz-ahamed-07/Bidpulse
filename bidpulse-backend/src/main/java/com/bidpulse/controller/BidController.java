package com.bidpulse.controller;

import com.bidpulse.dto.bid.BidDto;
import com.bidpulse.dto.bid.PlaceBidRequest;
import com.bidpulse.service.BidService;
import com.bidpulse.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auctions/{auctionId}/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;
    private final CurrentUserService currentUserService; // <-- Inject our new security tool

    @PostMapping
    public ResponseEntity<?> placeBid(@PathVariable Long auctionId,
                                      @RequestBody PlaceBidRequest req) {
        
        // Magically extract the user ID from the Axios JWT token!
        Long userId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new SecurityException("unauthenticated"));
                
        BidDto dto = bidService.placeBid(auctionId, userId, req);
        return ResponseEntity.status(201).body(dto);
    }
}
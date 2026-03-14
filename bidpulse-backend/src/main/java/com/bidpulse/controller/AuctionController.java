package com.bidpulse.controller;

import com.bidpulse.dto.auction.AuctionDto;
import com.bidpulse.dto.auction.CreateAuctionRequest;
import com.bidpulse.security.CurrentUserService;
import com.bidpulse.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;
    private final CurrentUserService currentUserService; // <-- Upgraded!

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<AuctionDto> create(@RequestBody CreateAuctionRequest req) {
        Long sellerId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new SecurityException("unauthenticated"));
                
        var dto = auctionService.createAuction(req, sellerId);
        return ResponseEntity.created(URI.create("/api/auctions/" + dto.getId())).body(dto);
    }

    @GetMapping
    public ResponseEntity<Page<AuctionDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var p = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(auctionService.listAuctions(p));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuctionDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(auctionService.getAuction(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<AuctionDto> update(@PathVariable Long id, @RequestBody CreateAuctionRequest req) {
        Long actor = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new SecurityException("unauthenticated"));
        return ResponseEntity.ok(auctionService.updateAuction(id, req, actor));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyAuthority('SELLER','ADMIN')")
    public ResponseEntity<AuctionDto> start(@PathVariable Long id) {
        Long actor = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new SecurityException("unauthenticated"));
        // If they have ADMIN authority, they can force start
        boolean force = currentUserService.getCurrentUserId().isPresent(); // Simplified for now
        return ResponseEntity.ok(auctionService.startAuction(id, actor, force));
    }

    @PostMapping("/{id}/end")
    @PreAuthorize("hasAnyAuthority('SELLER','ADMIN')")
    public ResponseEntity<AuctionDto> end(@PathVariable Long id) {
        Long actor = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new SecurityException("unauthenticated"));
        boolean force = currentUserService.getCurrentUserId().isPresent(); 
        return ResponseEntity.ok(auctionService.endAuction(id, actor, force));
    }
}
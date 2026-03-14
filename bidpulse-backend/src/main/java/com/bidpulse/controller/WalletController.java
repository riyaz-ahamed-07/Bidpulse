package com.bidpulse.controller;

import com.bidpulse.security.CurrentUserService;
import com.bidpulse.service.WalletService;
import com.bidpulse.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final WalletRepository walletRepository; // <-- Add this to read the balance
    private final CurrentUserService currentUserService;

    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@RequestParam BigDecimal amount) {
        Long userId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new SecurityException("unauthenticated"));
        
        walletService.deposit(userId, amount);
        return ResponseEntity.ok("Successfully deposited $" + amount + " into your wallet!");
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyBalance() {
        Long userId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new SecurityException("unauthenticated"));
                
        var wallet = walletRepository.findByUserId(userId).orElse(null);
        if (wallet == null) return ResponseEntity.status(404).body(Map.of("error", "Wallet not found"));
        
        // Return both the available balance and the money currently locked in active bids
        return ResponseEntity.ok(Map.of(
                "balance", wallet.getBalance(),
                "reserved", wallet.getReservedAmount()
        ));
    }
}
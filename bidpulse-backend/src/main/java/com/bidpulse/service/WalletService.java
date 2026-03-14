package com.bidpulse.service;

import java.math.BigDecimal;

public interface WalletService {
    /**
     * Reserve amount for a bid. Should throw an exception (ResponseStatusException) on insufficient funds.
     */
    void reserve(Long userId, java.math.BigDecimal amount, Long auctionId);

    /**
     * Release reserved amount (used when outbid).
     */
    void release(Long userId, java.math.BigDecimal amount, Long auctionId);

    /**
     * Charge the reserved funds (used when auction finalizes and winner pays).
     */
    void chargeReserved(Long userId, java.math.BigDecimal amount, Long auctionId);

    void deposit(Long userId, BigDecimal amount);
}
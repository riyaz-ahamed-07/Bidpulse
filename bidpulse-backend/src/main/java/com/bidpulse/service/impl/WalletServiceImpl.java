package com.bidpulse.service.impl;

import com.bidpulse.model.Wallet;
import com.bidpulse.model.PaymentTransaction;
import com.bidpulse.model.TransactionType;
import com.bidpulse.model.PaymentStatus;
import com.bidpulse.repository.WalletRepository;
import com.bidpulse.repository.PaymentTransactionRepository;
import com.bidpulse.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    @Override
    @Transactional
    public void reserve(Long userId, BigDecimal amount, Long auctionId) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid amount");
        }

        Wallet w = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));

        BigDecimal available = w.getBalance().subtract(w.getReservedAmount());
        if (available.compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }

        w.setReservedAmount(w.getReservedAmount().add(amount));
        walletRepository.save(w);

        // record transaction
        PaymentTransaction tx = PaymentTransaction.builder()
                .user(w.getUser())
                .auction(null)
                .type(TransactionType.HOLD)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .metadata("reserve for auction " + auctionId)
                .build();
        paymentTransactionRepository.save(tx);
    }

    @Override
    @Transactional
    public void release(Long userId, BigDecimal amount, Long auctionId) {
        Wallet w = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));

        BigDecimal reserved = w.getReservedAmount();
        BigDecimal toRelease = amount.min(reserved);
        w.setReservedAmount(reserved.subtract(toRelease));
        walletRepository.save(w);

        PaymentTransaction tx = PaymentTransaction.builder()
                .user(w.getUser())
                .auction(null)
                .type(TransactionType.RELEASE)
                .amount(toRelease)
                .status(PaymentStatus.COMPLETED)
                .metadata("release for auction " + auctionId)
                .build();
        paymentTransactionRepository.save(tx);
    }

    @Override
    @Transactional
    public void chargeReserved(Long userId, BigDecimal amount, Long auctionId) {
        Wallet w = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));

        BigDecimal reserved = w.getReservedAmount();
        if (reserved.compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Reserved amount insufficient for charge");
        }

        // move reserved -> reduce balance
        w.setReservedAmount(reserved.subtract(amount));
        w.setBalance(w.getBalance().subtract(amount));
        walletRepository.save(w);

        PaymentTransaction tx = PaymentTransaction.builder()
                .user(w.getUser())
                .auction(null)
                .type(TransactionType.CHARGE)
                .amount(amount)
                .status(PaymentStatus.COMPLETED)
                .metadata("charge for auction " + auctionId)
                .build();
        paymentTransactionRepository.save(tx);
    }
    @Override
    @Transactional
    public void deposit(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid deposit amount");
        }

        Wallet w = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));

        // Add the paper money to the balance
        w.setBalance(w.getBalance().add(amount));
        walletRepository.save(w);

        // Record the deposit transaction
        PaymentTransaction tx = PaymentTransaction.builder()
                .user(w.getUser())
                .auction(null)
                .type(TransactionType.DEPOSIT) // Make sure you have DEPOSIT in your TransactionType enum!
                .amount(amount)
                .status(PaymentStatus.COMPLETED)
                .metadata("Paper money deposit")
                .build();
        paymentTransactionRepository.save(tx);
    }
}
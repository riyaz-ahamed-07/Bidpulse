package com.bidpulse.service.impl;

import com.bidpulse.dto.auth.RegisterRequest;
import com.bidpulse.model.User;
import com.bidpulse.model.Wallet;
import com.bidpulse.repository.UserRepository;
import com.bidpulse.repository.WalletRepository;
import com.bidpulse.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder; // Import this
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder; // 1. Inject the encoder

    @Override
    @Transactional
    public User createUser(RegisterRequest req) {
        // simple no-duplicate check
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new IllegalArgumentException("email already exists");
        }

        User u = User.builder()
                .email(req.getEmail())
                .name(req.getName())
                // 2. Use the encoder here
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .roles(java.util.Set.of("USER"))
                .build();
        u = userRepository.save(u);

        // create wallet row with zero balance so later wallet logic works
        Wallet w = Wallet.builder()
                .user(u)
                .balance(BigDecimal.ZERO)
                .reservedAmount(BigDecimal.ZERO)
                .build();
        walletRepository.save(w);

        return u;
    }
}
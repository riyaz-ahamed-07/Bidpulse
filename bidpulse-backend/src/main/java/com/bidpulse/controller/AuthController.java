package com.bidpulse.controller;

import com.bidpulse.model.RefreshToken;
import com.bidpulse.model.User;
import com.bidpulse.repository.RefreshTokenRepository;
import com.bidpulse.repository.UserRepository;
import com.bidpulse.security.JwtService;
import com.bidpulse.util.TokenHash;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.access-expiry-seconds:900}")
    private long accessExpirySeconds;

    @Value("${app.jwt.refresh-expiry-seconds:1209600}")
    private long refreshExpirySeconds;

    record LoginRequest(String email, String password) {}
    record TokenResponse(String accessToken, String refreshToken, long expiresIn) {}

    @PostMapping("/login")
    public ResponseEntity<?> sessionLogin(@RequestBody LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        return ResponseEntity.ok(Map.of("status","ok"));
    }

    @PostMapping("/token")
    public ResponseEntity<?> tokenLogin(@RequestBody LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        User user = userRepository.findByEmail(req.email()).orElseThrow();

        String rolesCsv = String.join(",", user.getRoles());
        String access = jwtService.generateAccessToken(user.getId(), user.getEmail(), rolesCsv);
        String refresh = jwtService.generateRefreshToken();

        // hash and store refresh token
        String hash = TokenHash.sha256Hex(refresh);
        RefreshToken rt = RefreshToken.builder()
                .tokenHash(hash)
                .user(user)
                .expiry(Instant.now().plusSeconds(refreshExpirySeconds))
                .revoked(false)
                .build();
        refreshTokenRepository.save(rt);

        return ResponseEntity.ok(new TokenResponse(access, refresh, accessExpirySeconds));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String,String> body) {
        String refresh = body.get("refreshToken");
        if (refresh == null) return ResponseEntity.badRequest().body(Map.of("error","missing_refreshToken"));
        String hash = TokenHash.sha256Hex(refresh);
        var rtOpt = refreshTokenRepository.findByTokenHash(hash);
        if (rtOpt.isEmpty()) return ResponseEntity.status(401).body(Map.of("error","invalid_refresh"));
        var rt = rtOpt.get();
        if (rt.isRevoked() || rt.getExpiry().isBefore(Instant.now())) {
            return ResponseEntity.status(401).body(Map.of("error","expired_or_revoked"));
        }
        User user = rt.getUser();
        String rolesCsv = String.join(",", user.getRoles());
        String access = jwtService.generateAccessToken(user.getId(), user.getEmail(), rolesCsv);
        return ResponseEntity.ok(Map.of("accessToken", access));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody(required = false) Map<String,String> body) {
        if (body != null && body.containsKey("refreshToken")) {
            String hash = TokenHash.sha256Hex(body.get("refreshToken"));
            refreshTokenRepository.findByTokenHash(hash).ifPresent(rt -> {
                rt.setRevoked(true);
                refreshTokenRepository.save(rt);
            });
        }
        return ResponseEntity.ok(Map.of("status","logged_out"));
    }
}
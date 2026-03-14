package com.bidpulse.controller;

import com.bidpulse.dto.auth.RegisterRequest;
import com.bidpulse.model.User;
import com.bidpulse.repository.UserRepository;
import com.bidpulse.security.CurrentUserService;
import com.bidpulse.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        User created = userService.createUser(req);
        return ResponseEntity.status(201).body(
            Map.of("id", created.getId(), "email", created.getEmail())
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        // Use our custom service to cleanly grab the ID from the token
        Long userId = currentUserService.getCurrentUserId().orElse(null);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "unauthenticated"));

        User u = userRepository.findById(userId).orElse(null);
        if (u == null) return ResponseEntity.status(404).body(Map.of("error", "user not found"));

        return ResponseEntity.ok(Map.of(
                "id", u.getId(),
                "email", u.getEmail(),
                "name", u.getName(),
                "roles", u.getRoles()
        ));
    }
}
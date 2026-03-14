package com.bidpulse.controller;

import com.bidpulse.model.ApplicationStatus;
import com.bidpulse.model.SellerApplication;
import com.bidpulse.model.User;
import com.bidpulse.repository.SellerApplicationRepository;
import com.bidpulse.repository.UserRepository;
import com.bidpulse.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SellerApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    // 1. FOR NORMAL USERS: Submit an application
    @PostMapping("/apply-seller")
    public ResponseEntity<?> applyToSell(@RequestBody Map<String, String> payload) {
        Long userId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new SecurityException("unauthenticated"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if they already applied
        if (applicationRepository.findByUserIdAndStatus(userId, ApplicationStatus.PENDING).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "You already have a pending application!"));
        }

        SellerApplication app = SellerApplication.builder()
                .user(user)
                .reason(payload.getOrDefault("reason", "I want to sell items."))
                .build();

        applicationRepository.save(app);
        return ResponseEntity.ok(Map.of("message", "Application submitted successfully!"));
    }

    // 2. FOR ADMINS: View all pending applications
    @GetMapping("/applications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingApplications() {
        // Find all applications where status is PENDING
        List<SellerApplication> pendingApps = applicationRepository.findByStatus(ApplicationStatus.PENDING);
        
        // Map them to a clean JSON response
        var response = pendingApps.stream().map(app -> Map.of(
                "applicationId", app.getId(),
                "userId", app.getUser().getId(),
                "userName", app.getUser().getName(),
                "userEmail", app.getUser().getEmail(),
                "reason", app.getReason(),
                "createdAt", app.getCreatedAt()
        )).toList();

        return ResponseEntity.ok(response);
    }

    // 3. FOR ADMINS: Approve an application
    @PostMapping("/applications/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveApplication(@PathVariable Long id) {
        SellerApplication app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (app.getStatus() != ApplicationStatus.PENDING) {
            return ResponseEntity.badRequest().body(Map.of("message", "Application is not pending."));
        }

        // 1. Mark application as APPROVED
        app.setStatus(ApplicationStatus.APPROVED);
        applicationRepository.save(app);

        // 2. Grant the SELLER role to the user!
        User user = app.getUser();
        if (!user.getRoles().contains("SELLER")) {
            user.getRoles().add("SELLER");
            userRepository.save(user);
        }

        return ResponseEntity.ok(Map.of("message", "User " + user.getName() + " is now a SELLER!"));
    }
}
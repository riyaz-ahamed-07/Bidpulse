package com.bidpulse.security;

import com.bidpulse.model.User;
import com.bidpulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final UserRepository userRepository;

    /**
     * Returns current user id by looking up the authenticated principal's username (which we use as email).
     * This is a simple approach that works for both session-based UsernamePasswordAuthentication and JWT
     * if Authentication.getName() returns the principal's email.
     */
    public Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        String username = auth.getName(); // typically email
        User user = userRepository.findByEmail(username).orElse(null);
        return user != null ? user.getId() : null;
    }

    public boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}
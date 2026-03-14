package com.bidpulse.security;

import com.bidpulse.model.User;
import com.bidpulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("user not found: " + username));

        var authorities = u.getRoles().stream()
            .map(r -> {
                String role = r.startsWith("ROLE_") ? r : "ROLE_" + r;
                return new SimpleGrantedAuthority(role);
            })
            .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                u.getEmail(),
                u.getPasswordHash(),
                authorities
        );
    }
}
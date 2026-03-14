package com.bidpulse.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, jakarta.servlet.ServletException {

        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            String token = h.substring(7);
            try {
                Jws<Claims> j = jwtService.parseToken(token);
                Claims claims = j.getBody();
                String email = claims.get("email", String.class);
                
                // 1. SAFELY EXTRACT ROLES (Handles both Strings and Arrays)
                List<String> rolesList = new ArrayList<>();
                Object rolesObj = claims.get("roles");
                
                if (rolesObj instanceof String) {
                    rolesList = List.of(((String) rolesObj).split(","));
                } else if (rolesObj instanceof List) {
                    rolesList = (List<String>) rolesObj;
                }

                // 2. FORCE THE 'ROLE_' PREFIX
                List<SimpleGrantedAuthority> authorities = rolesList.stream()
                        .map(r -> r.trim().toUpperCase())
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                // 3. THE WIRETAP: Print to the Spring Boot terminal!
                System.out.println("🛡️ JWT Bouncer checking user: " + email);
                System.out.println("🛡️ Granted Authorities: " + authorities);

                if (email != null) {
                    var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                System.out.println("❌ JWT Bouncer Error: " + e.getMessage());
            }
        }
        chain.doFilter(req, res);
    }
}
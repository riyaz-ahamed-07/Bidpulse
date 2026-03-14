 package com.bidpulse.security;


import com.bidpulse.model.User;

import com.bidpulse.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;


import java.util.Optional;


@Service

@RequiredArgsConstructor

public class CurrentUserService {

private final UserRepository userRepository;


// Returns user id if authenticated, otherwise empty

public Optional<Long> getCurrentUserId() {

Authentication auth = SecurityContextHolder.getContext().getAuthentication();

if (auth == null || !auth.isAuthenticated() || auth.getName() == null) return Optional.empty();

String email = auth.getName(); // we set principal as email earlier

return userRepository.findByEmail(email).map(User::getId);

}

} 
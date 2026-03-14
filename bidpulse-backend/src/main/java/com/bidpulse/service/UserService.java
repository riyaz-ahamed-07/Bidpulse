package com.bidpulse.service;

import com.bidpulse.dto.auth.RegisterRequest;
import com.bidpulse.model.User;

public interface UserService {
    User createUser(RegisterRequest req);
}
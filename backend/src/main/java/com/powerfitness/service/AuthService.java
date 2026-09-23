package com.powerfitness.service;

import com.powerfitness.dto.AuthResponse;
import com.powerfitness.dto.LoginRequest;
import com.powerfitness.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(String refreshToken);

    void logout(String refreshToken);
}

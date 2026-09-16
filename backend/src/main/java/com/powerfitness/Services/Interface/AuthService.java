package com.powerfitness.Services.Interface;

import com.powerfitness.DTO.AuthResponse;
import com.powerfitness.DTO.LoginRequest;
import com.powerfitness.DTO.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(String refreshToken);

    void logout(String refreshToken);
}

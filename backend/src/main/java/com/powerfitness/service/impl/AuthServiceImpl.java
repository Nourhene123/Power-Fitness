package com.powerfitness.service.impl;

import com.powerfitness.dto.AuthResponse;
import com.powerfitness.dto.LoginRequest;
import com.powerfitness.dto.RegisterRequest;
import com.powerfitness.dto.UserDto;
import com.powerfitness.entity.RefreshToken;
import com.powerfitness.entity.Role;
import com.powerfitness.entity.User;
import com.powerfitness.exception.EmailAlreadyUsedException;
import com.powerfitness.exception.InvalidCredentialsException;
import com.powerfitness.mapper.UserMapper;
import com.powerfitness.repository.AssessmentRepository;
import com.powerfitness.repository.UserRepository;
import com.powerfitness.security.JwtService;
import com.powerfitness.service.AuthService;
import com.powerfitness.service.RefreshTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository users;
    private final AssessmentRepository assessments;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokens;
    private final UserMapper userMapper;

    public AuthServiceImpl(UserRepository users, AssessmentRepository assessments,
                           PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                           JwtService jwtService, RefreshTokenService refreshTokens, UserMapper userMapper) {
        this.users = users;
        this.assessments = assessments;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokens = refreshTokens;
        this.userMapper = userMapper;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyUsedException(email);
        }
        User user = users.save(User.builder()
                .name(request.name().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build());
        return issueFor(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.email().trim().toLowerCase(), request.password()));
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        User user = users.findByEmailIgnoreCase(request.email().trim().toLowerCase())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
        return issueFor(user);
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        RefreshToken current = refreshTokens.verifyActive(refreshToken);
        User user = current.getUser();
        String rotated = refreshTokens.rotate(current);
        return new AuthResponse(
                jwtService.generateAccessToken(user),
                rotated,
                jwtService.accessTokenTtlSeconds(),
                toDto(user));
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokens.revoke(refreshToken);
        }
    }

    private AuthResponse issueFor(User user) {
        return new AuthResponse(
                jwtService.generateAccessToken(user),
                refreshTokens.issue(user),
                jwtService.accessTokenTtlSeconds(),
                toDto(user));
    }

    private UserDto toDto(User user) {
        return userMapper.toDto(user, assessments.existsByUser(user));
    }
}

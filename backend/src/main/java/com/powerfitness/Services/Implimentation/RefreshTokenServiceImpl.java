package com.powerfitness.Services.Implimentation;

import com.powerfitness.Config.AppProperties;
import com.powerfitness.Entity.RefreshToken;
import com.powerfitness.Entity.User;
import com.powerfitness.Exception.InvalidCredentialsException;
import com.powerfitness.Repository.RefreshTokenRepository;
import com.powerfitness.Services.Interface.RefreshTokenService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder URL64 = Base64.getUrlEncoder().withoutPadding();

    private final RefreshTokenRepository repository;
    private final AppProperties props;

    public RefreshTokenServiceImpl(RefreshTokenRepository repository, AppProperties props) {
        this.repository = repository;
        this.props = props;
    }

    @Override
    public String issue(User user) {
        byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        String token = URL64.encodeToString(raw);

        repository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(hash(token))
                .expiresAt(Instant.now().plus(props.jwt().refreshTokenTtl()))
                .revoked(false)
                .build());
        return token;
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken verifyActive(String rawToken) {
        RefreshToken token = repository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));
        if (!token.isActive()) {
            throw new InvalidCredentialsException("Refresh token expired or revoked");
        }
        return token;
    }

    @Override
    public String rotate(RefreshToken current) {
        current.setRevoked(true);
        repository.save(current);
        return issue(current.getUser());
    }

    @Override
    public void revoke(String rawToken) {
        repository.findByTokenHash(hash(rawToken)).ifPresent(t -> {
            t.setRevoked(true);
            repository.save(t);
        });
    }

    @Override
    public void revokeAll(User user) {
        repository.revokeAllForUser(user);
    }

    private static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

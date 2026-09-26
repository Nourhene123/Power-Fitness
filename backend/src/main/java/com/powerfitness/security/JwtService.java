package com.powerfitness.security;

import com.powerfitness.config.AppProperties;
import com.powerfitness.entity.Role;
import com.powerfitness.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final Duration accessTtl;

    /** The dev-only default from application.yml; it's public (in the repo), so never valid in prod. */
    static final String DEV_SECRET_MARKER = "dev-secret-please-change-me";

    public JwtService(AppProperties props, Environment environment) {
        String secret = props.jwt().secret();
        // An unset env var reaches us as the literal placeholder text ("${APP_JWT_SECRET}").
        if (secret == null || secret.isBlank() || secret.contains("${")) {
            throw new IllegalStateException("APP_JWT_SECRET is not set; the app can't sign tokens without it.");
        }
        if (environment.acceptsProfiles(Profiles.of("prod")) && secret.contains(DEV_SECRET_MARKER)) {
            throw new IllegalStateException(
                    "APP_JWT_SECRET is the public dev default; set a real secret for the prod profile.");
        }
        // hmacShaKeyFor also rejects secrets shorter than 256 bits (32 bytes) with a WeakKeyException.
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = props.jwt().issuer();
        this.accessTtl = props.jwt().accessTokenTtl();
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTtl)))
                .signWith(key)
                .compact();
    }

    public long accessTokenTtlSeconds() {
        return accessTtl.toSeconds();
    }

  
    public Optional<AppUserPrincipal> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(new AppUserPrincipal(
                    Long.valueOf(claims.getSubject()),
                    claims.get("email", String.class),
                    Role.valueOf(claims.get("role", String.class)),
                    null));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}

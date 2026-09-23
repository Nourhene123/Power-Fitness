package com.powerfitness.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.powerfitness.config.AppProperties;
import com.powerfitness.entity.RefreshToken;
import com.powerfitness.entity.Role;
import com.powerfitness.entity.User;
import com.powerfitness.exception.InvalidCredentialsException;
import com.powerfitness.repository.RefreshTokenRepository;
import com.powerfitness.service.impl.RefreshTokenServiceImpl;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pure unit tests for the token issue/rotate/revoke lifecycle, with the repository mocked. See
 * {@code AuthControllerIntegrationTest} for rotation and revocation verified through real HTTP
 * calls against a real database.
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock private RefreshTokenRepository repository;

    private RefreshTokenServiceImpl service;

    @BeforeEach
    void setUp() {
        AppProperties.Jwt jwt = new AppProperties.Jwt("secret", Duration.ofMinutes(15), Duration.ofDays(30), "issuer");
        AppProperties props = new AppProperties(jwt, null, null);
        service = new RefreshTokenServiceImpl(repository, props);
    }

    private static String sha256Hex(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static User aUser() {
        User user = User.builder().name("Jane").email("jane@example.com").password("hashed").role(Role.USER).build();
        user.setId(1L);
        return user;
    }

    @Test
    void issueStoresOnlyTheHashOfTheTokenAndReturnsTheRawTokenToTheCaller() {
        User user = aUser();

        String raw = service.issue(user);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository).save(captor.capture());
        RefreshToken saved = captor.getValue();

        assertThat(raw).isNotBlank();
        assertThat(saved.getTokenHash()).isEqualTo(sha256Hex(raw));
        assertThat(saved.getTokenHash()).isNotEqualTo(raw);
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.isRevoked()).isFalse();
        assertThat(saved.getExpiresAt()).isAfter(Instant.now().plus(Duration.ofDays(29)));
    }

    @Test
    void verifyActiveReturnsTheTokenWhenItIsFoundAndStillActive() {
        RefreshToken token = RefreshToken.builder()
                .user(aUser()).tokenHash(sha256Hex("raw-token"))
                .expiresAt(Instant.now().plusSeconds(60)).revoked(false).build();
        when(repository.findByTokenHash(sha256Hex("raw-token"))).thenReturn(Optional.of(token));

        RefreshToken result = service.verifyActive("raw-token");

        assertThat(result).isSameAs(token);
    }

    @Test
    void verifyActiveThrowsWhenNoTokenMatchesTheHash() {
        when(repository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verifyActive("unknown-token"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void verifyActiveThrowsWhenTheMatchingTokenIsExpiredOrRevoked() {
        RefreshToken expired = RefreshToken.builder()
                .user(aUser()).tokenHash(sha256Hex("expired")).expiresAt(Instant.now().minusSeconds(1)).revoked(false).build();
        when(repository.findByTokenHash(sha256Hex("expired"))).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service.verifyActive("expired"))
                .isInstanceOf(InvalidCredentialsException.class);

        RefreshToken revoked = RefreshToken.builder()
                .user(aUser()).tokenHash(sha256Hex("revoked")).expiresAt(Instant.now().plusSeconds(60)).revoked(true).build();
        when(repository.findByTokenHash(sha256Hex("revoked"))).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> service.verifyActive("revoked"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void rotateRevokesTheCurrentTokenAndIssuesAReplacementForTheSameUser() {
        User user = aUser();
        RefreshToken current = RefreshToken.builder()
                .user(user).tokenHash(sha256Hex("current")).expiresAt(Instant.now().plusSeconds(60)).revoked(false).build();

        String rotated = service.rotate(current);

        assertThat(current.isRevoked()).isTrue();
        assertThat(rotated).isNotBlank();
        // one save() to persist the revoked flag on `current`, one to persist the freshly issued token
        verify(repository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    void revokeMarksTheMatchingTokenAsRevoked() {
        RefreshToken token = RefreshToken.builder()
                .user(aUser()).tokenHash(sha256Hex("raw")).expiresAt(Instant.now().plusSeconds(60)).revoked(false).build();
        when(repository.findByTokenHash(sha256Hex("raw"))).thenReturn(Optional.of(token));

        service.revoke("raw");

        assertThat(token.isRevoked()).isTrue();
        verify(repository).save(token);
    }

    @Test
    void revokeIsANoOpWhenNoTokenMatchesTheHash() {
        when(repository.findByTokenHash(any())).thenReturn(Optional.empty());

        service.revoke("unknown");

        verify(repository, never()).save(any());
    }

    @Test
    void revokeAllDelegatesToTheRepositoryBulkUpdate() {
        User user = aUser();

        service.revokeAll(user);

        verify(repository).revokeAllForUser(user);
    }
}

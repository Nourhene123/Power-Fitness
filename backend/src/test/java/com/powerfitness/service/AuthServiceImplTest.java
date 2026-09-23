package com.powerfitness.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.powerfitness.service.impl.AuthServiceImpl;
import com.powerfitness.service.RefreshTokenService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

/*un test unitaire : il isole AuthServiceImpl (la classe qui contient la logique métier de l'authentification) en remplaçant toutes ses dépendances par des faux objets (mocks)
 — pas de vraie base de données, pas de vrai HTTP, pas de Spring qui démarre*/
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository users;
    @Mock private AssessmentRepository assessments;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenService refreshTokens;
    @Mock private UserMapper userMapper;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                users, assessments, passwordEncoder, authenticationManager, jwtService, refreshTokens, userMapper);
    }

    private static User savedUser(long id, String email, Role role) {
        User user = User.builder().name("Jane Doe").email(email).password("hashed").role(role).build();
        user.setId(id);
        return user;
    }

    @Test
    void registerCreatesAUserWithNormalizedEmailAndTrimmedName() {
        RegisterRequest request = new RegisterRequest("  Jane Doe  ", "  Jane@Example.com ", "SuperSecret1");
        when(users.existsByEmailIgnoreCase("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("SuperSecret1")).thenReturn("hashed");
        when(users.save(any(User.class))).thenAnswer(inv -> savedUser(1L, "jane@example.com", Role.USER));
        when(assessments.existsByUser(any(User.class))).thenReturn(false);
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.accessTokenTtlSeconds()).thenReturn(900L);
        when(refreshTokens.issue(any(User.class))).thenReturn("refresh-token");
        when(userMapper.toDto(any(User.class), eq(false)))
                .thenReturn(new UserDto(1L, "Jane Doe", "jane@example.com", "USER", null, false));

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(users).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Jane Doe");
        assertThat(saved.getEmail()).isEqualTo("jane@example.com");
        assertThat(saved.getPassword()).isEqualTo("hashed");
        assertThat(saved.getRole()).isEqualTo(Role.USER);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.expiresIn()).isEqualTo(900L);
        assertThat(response.user().email()).isEqualTo("jane@example.com");
    }

    @Test
    void registerThrowsWhenEmailIsAlreadyUsedAndNeverPersistsTheUser() {
        RegisterRequest request = new RegisterRequest("Jane", "jane@example.com", "SuperSecret1");
        when(users.existsByEmailIgnoreCase("jane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyUsedException.class);

        verify(users, never()).save(any());
    }

    @Test
    void loginAuthenticatesAndIssuesTokensOnValidCredentials() {
        LoginRequest request = new LoginRequest(" User@Example.com ", "password123");
        User user = savedUser(7L, "user@example.com", Role.USER);
        when(users.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(assessments.existsByUser(user)).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.accessTokenTtlSeconds()).thenReturn(900L);
        when(refreshTokens.issue(user)).thenReturn("refresh-token");
        when(userMapper.toDto(user, true))
                .thenReturn(new UserDto(7L, "Jane Doe", "user@example.com", "USER", null, true));

        AuthResponse response = authService.login(request);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo("user@example.com");
        assertThat(captor.getValue().getCredentials()).isEqualTo("password123");

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.user().hasAssessment()).isTrue();
    }

    @Test
    void loginThrowsInvalidCredentialsWhenAuthenticationManagerRejectsIt() {
        LoginRequest request = new LoginRequest("user@example.com", "wrong-password");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(users, never()).findByEmailIgnoreCase(anyString());
    }

    @Test
    void loginThrowsInvalidCredentialsWhenTheUserDisappearsAfterAuthenticating() {
        LoginRequest request = new LoginRequest("user@example.com", "password123");
        when(users.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refreshRotatesTheTokenAndReturnsAFreshAccessToken() {
        User user = savedUser(3L, "user@example.com", Role.COACH);
        RefreshToken current = RefreshToken.builder().user(user).tokenHash("hash").revoked(false).build();
        when(refreshTokens.verifyActive("old-refresh")).thenReturn(current);
        when(refreshTokens.rotate(current)).thenReturn("new-refresh");
        when(jwtService.generateAccessToken(user)).thenReturn("new-access");
        when(jwtService.accessTokenTtlSeconds()).thenReturn(900L);
        when(assessments.existsByUser(user)).thenReturn(false);
        when(userMapper.toDto(user, false))
                .thenReturn(new UserDto(3L, "Coach", "user@example.com", "COACH", null, false));

        AuthResponse response = authService.refresh("old-refresh");

        assertThat(response.accessToken()).isEqualTo("new-access");
        assertThat(response.refreshToken()).isEqualTo("new-refresh");
    }

    @Test
    void logoutRevokesTheGivenRefreshToken() {
        authService.logout("some-refresh-token");

        verify(refreshTokens).revoke("some-refresh-token");
    }

    @Test
    void logoutDoesNothingWhenNoRefreshTokenIsGiven() {
        authService.logout(null);
        authService.logout("   ");

        verify(refreshTokens, never()).revoke(anyString());
    }
}

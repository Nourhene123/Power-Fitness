package com.powerfitness.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.powerfitness.entity.Role;
import com.powerfitness.entity.User;
import com.powerfitness.repository.UserRepository;
import com.powerfitness.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
ce test d'intégration : il vérifie que le module Auth fonctionne correctement de bout en bout,
 à travers de vraies requêtes HTTP, une vraie base PostgreSQL, et la vraie chaîne de sécurité
 */
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private static String jsonField(String json, String field) {
        return JsonPath.read(json, "$." + field);
    }

    private User seedUser(String email, String rawPassword, Role role) {
        return userRepository.save(User.builder()
                .name("Existing User")
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .build());
    }

    @Test
    void registerCreatesAUserAndReturnsUsableTokens() throws Exception {
        String body = """
                {"name":"Jane Doe","email":"jane-register@example.com","password":"SuperSecret1"}""";

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", not("")))
                .andExpect(jsonPath("$.refreshToken", not("")))
                .andExpect(jsonPath("$.user.email").value("jane-register@example.com"))
                .andExpect(jsonPath("$.user.role").value("USER"))
                .andExpect(jsonPath("$.user.hasAssessment").value(false));

        assertThat(userRepository.existsByEmailIgnoreCase("jane-register@example.com")).isTrue();
    }

    @Test
    void registerRejectsAnEmailThatIsAlreadyRegistered() throws Exception {
        seedUser("taken@example.com", "SuperSecret1", Role.USER);
        String body = """
                {"name":"Someone Else","email":"taken@example.com","password":"AnotherPass1"}""";

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_USED"));
    }

    @Test
    void registerRejectsAnInvalidBodyWithFieldLevelErrors() throws Exception {
        String body = """
                {"name":"","email":"not-an-email","password":"short"}""";

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[*].field")
                        .value(org.hamcrest.Matchers.containsInAnyOrder("name", "email", "password")));
    }

    @Test
    void loginReturnsTokensForValidCredentials() throws Exception {
        seedUser("login-ok@example.com", "CorrectHorse1", Role.USER);
        String body = """
                {"email":"login-ok@example.com","password":"CorrectHorse1"}""";

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not("")))
                .andExpect(jsonPath("$.user.email").value("login-ok@example.com"));
    }

    @Test
    void loginRejectsAWrongPasswordWithoutRevealingWhichFieldWasWrong() throws Exception {
        seedUser("login-badpw@example.com", "CorrectHorse1", Role.USER);
        String body = """
                {"email":"login-badpw@example.com","password":"WrongPassword1"}""";

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void loginRejectsAnUnknownEmail() throws Exception {
        String body = """
                {"email":"nobody-here@example.com","password":"WhateverPass1"}""";

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void refreshRotatesTheTokenAndTheOldRefreshTokenCanNoLongerBeUsed() throws Exception {
        seedUser("refresh-flow@example.com", "CorrectHorse1", Role.USER);
        String loginBody = """
                {"email":"refresh-flow@example.com","password":"CorrectHorse1"}""";
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String originalRefreshToken = jsonField(loginResponse, "refreshToken");

        String refreshBody = "{\"refreshToken\":\"" + originalRefreshToken + "\"}";
        String refreshResponse = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON).content(refreshBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not("")))
                .andReturn().getResponse().getContentAsString();
        String rotatedRefreshToken = jsonField(refreshResponse, "refreshToken");
        assertThat(rotatedRefreshToken).isNotEqualTo(originalRefreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON).content(refreshBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void refreshRejectsAGarbageToken() throws Exception {
        String body = """
                {"refreshToken":"this-token-does-not-exist"}""";

        mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void logoutRevokesTheRefreshTokenSoItCanNoLongerBeUsed() throws Exception {
        seedUser("logout-flow@example.com", "CorrectHorse1", Role.USER);
        String loginBody = """
                {"email":"logout-flow@example.com","password":"CorrectHorse1"}""";
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String accessToken = jsonField(loginResponse, "accessToken");
        String refreshToken = jsonField(loginResponse, "refreshToken");
        String logoutBody = "{\"refreshToken\":\"" + refreshToken + "\"}";

        // logout requires a valid access token, same as every other authed endpoint — the
        // frontend's auth interceptor attaches one automatically (it's not in its AUTH_SKIP list)
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON).content(logoutBody))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(logoutBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meReturnsTheAuthenticatedUserWhenGivenAValidAccessToken() throws Exception {
        seedUser("me-flow@example.com", "CorrectHorse1", Role.USER);
        String loginBody = """
                {"email":"me-flow@example.com","password":"CorrectHorse1"}""";
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String accessToken = jsonField(loginResponse, "accessToken");

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("me-flow@example.com"));
    }

    @Test
    void meRejectsRequestsWithoutAnAccessToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }
}

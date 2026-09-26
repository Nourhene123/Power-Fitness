package com.powerfitness.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.powerfitness.config.AppProperties;
import io.jsonwebtoken.security.WeakKeyException;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

/** Startup guards on the JWT signing secret. */
class JwtServiceTest {

    private static final String DEV_DEFAULT = "dev-secret-please-change-me-0123456789abcdef";
    private static final String STRONG = "a-real-production-secret-with-more-than-32-bytes";

    private static AppProperties props(String secret) {
        return new AppProperties(
                new AppProperties.Jwt(secret, Duration.ofMinutes(15), Duration.ofDays(30), "powerfitness"),
                new AppProperties.Cors(List.of()),
                new AppProperties.Uploads("./uploads"));
    }

    private static MockEnvironment profile(String name) {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles(name);
        return env;
    }

    @Test
    void prodRefusesToStartWithThePublicDevSecret() {
        assertThatThrownBy(() -> new JwtService(props(DEV_DEFAULT), profile("prod")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_JWT_SECRET");
    }

    @Test
    void aMissingSecretFailsWithAClearMessage() {
        assertThatThrownBy(() -> new JwtService(props("${APP_JWT_SECRET}"), profile("prod")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_JWT_SECRET is not set");
    }

    @Test
    void prodStartsWithARealSecret() {
        assertThatCode(() -> new JwtService(props(STRONG), profile("prod"))).doesNotThrowAnyException();
    }

    @Test
    void devMayUseTheDevSecret() {
        assertThatCode(() -> new JwtService(props(DEV_DEFAULT), profile("dev"))).doesNotThrowAnyException();
    }

    @Test
    void aSecretShorterThan256BitsIsRejected() {
        assertThatThrownBy(() -> new JwtService(props("too-short"), profile("prod")))
                .isInstanceOf(WeakKeyException.class);
    }
}

package com.powerfitness.Config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Cors cors, Uploads uploads) {

    public record Jwt(
            String secret,
            Duration accessTokenTtl,
            Duration refreshTokenTtl,
            String issuer) {}

    public record Cors(List<String> allowedOrigins) {}

    public record Uploads(String dir) {}
}

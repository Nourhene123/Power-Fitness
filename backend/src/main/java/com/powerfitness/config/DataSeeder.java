package com.powerfitness.config;

import com.powerfitness.entity.Role;
import com.powerfitness.entity.User;
import com.powerfitness.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String COACH_EMAIL = "coach@powerfitness.com";
    private static final String ADMIN_EMAIL = "admin@powerfitness.com";

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seed(COACH_EMAIL, "Head Coach Ilyes", "PowerCoach123!", Role.COACH);
        seed(ADMIN_EMAIL, "Site Admin", "PowerAdmin123!", Role.ADMIN);
    }

    private void seed(String email, String name, String rawPassword, Role role) {
        if (users.existsByEmailIgnoreCase(email)) {
            return;
        }
        users.save(User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .build());
        log.info("Seeded demo {} {} / {}", role.name().toLowerCase(), email, rawPassword);
    }
}

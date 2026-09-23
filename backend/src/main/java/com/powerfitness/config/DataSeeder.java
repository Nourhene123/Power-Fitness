package com.powerfitness.config;

import com.powerfitness.entity.Role;
import com.powerfitness.entity.User;
import com.powerfitness.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "demo"})
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String COACH_EMAIL = "coach@powerfitness.com";
    private static final String ADMIN_EMAIL = "admin@powerfitness.com";

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;

    // The coach account is public (it's the demo login); the admin one must not be, so on a
    // public deployment its password comes from APP_SEED_ADMIN_PASSWORD instead of this file.
    public DataSeeder(UserRepository users, PasswordEncoder passwordEncoder,
                      @Value("${app.seed.admin-password:PowerAdmin123!}") String adminPassword) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        seed(COACH_EMAIL, "Head Coach Ilyes", "PowerCoach123!", Role.COACH);
        seed(ADMIN_EMAIL, "Site Admin", adminPassword, Role.ADMIN);
        syncPassword(ADMIN_EMAIL, adminPassword);
    }

    /** Keeps an already-seeded account's password in line with the configured one. */
    private void syncPassword(String email, String rawPassword) {
        users.findByEmailIgnoreCase(email)
                .filter(user -> !passwordEncoder.matches(rawPassword, user.getPassword()))
                .ifPresent(user -> {
                    user.setPassword(passwordEncoder.encode(rawPassword));
                    users.save(user);
                    log.info("Updated seeded password for {}", email);
                });
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
        log.info("Seeded demo {} {}", role.name().toLowerCase(), email);
    }
}

package com.powerfitness.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for tests that need the full application context backed by a real PostgreSQL
 * instance (Flyway runs against it).
 *
 * <p>The container is started once, manually, in a static initializer — not via
 * {@code @Testcontainers}/{@code @Container} — because that JUnit extension ties a static
 * container's lifecycle to whichever single test class it's declared in and stops it in that
 * class's {@code afterAll}. Multiple subclasses sharing this one static field would then have
 * the second class try to connect to a container the first class already tore down. Starting it
 * once here and never stopping it (Ryuk reaps it when the JVM exits) is the standard "singleton
 * container" pattern for a base class meant to be extended by more than one test class.
 */
@ActiveProfiles("test")
@SpringBootTest
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }
}

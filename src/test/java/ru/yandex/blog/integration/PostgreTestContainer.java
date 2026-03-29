package ru.yandex.blog.integration;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;

public final class PostgreTestContainer {

    @Container
    @ServiceConnection
    private static final org.testcontainers.containers.PostgreSQLContainer<?> POSTGRES = new org.testcontainers.containers.PostgreSQLContainer<>("postgres:17")
        .withDatabaseName("testdb")
        .withUsername("testuser")
        .withPassword("testpass");
}

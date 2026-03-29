package ru.yandex.blog.integration;

import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Path;

public class DynamicIntegrationTempDir {

    @TempDir
    private static Path TEMP_DIR;

    @DynamicPropertySource
    private static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("file.dir", TEMP_DIR::toString);
    }
}

package ru.yandex.blog.integration.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.yandex.blog.configuration.integration.service.IntegrationServiceConfiguration;
import ru.yandex.blog.dto.post.PostCreateRequestDto;
import ru.yandex.blog.dto.post.PostResponseDto;
import ru.yandex.blog.integration.DynamicIntegrationTempDir;
import ru.yandex.blog.service.PostFileService;
import ru.yandex.blog.service.PostService;
import ru.yandex.blog.integration.PostgreTestContainer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

@SpringJUnitConfig(classes = IntegrationServiceConfiguration.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Testcontainers
@ImportTestcontainers(PostgreTestContainer.class)
@Sql(scripts = "classpath:/sql/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
@Sql(scripts = "classpath:/sql/schema.sql", executionPhase = BEFORE_TEST_CLASS)
class IntegrationPostFileServiceTest extends DynamicIntegrationTempDir {

    private final PostFileService fileService;

    private final PostService postService;

    private final MockMultipartFile file = new MockMultipartFile(
        "file",
        null,
        IMAGE_PNG_VALUE,
        new byte[]{1, 2, 3, 4}
    );

    private final List<String> tags = List.of("JAVA", "python");

    private final String postTitle = "Tes1";

    private final String postText = "Text";

    private final PostCreateRequestDto requestDto = PostCreateRequestDto.builder()
        .title(postTitle)
        .text(postText)
        .tags(tags)
        .build();

    @TempDir
    private static Path TEMP_DIR;

    @DynamicPropertySource
    private static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("file.dir", TEMP_DIR::toString);
    }

    @Test
    void shouldUpdatePostFileAndGet() throws IOException {
        PostResponseDto response = postService.save(requestDto);
        assertNotNull(response);
        assertTrue(response.getTags().contains(tags.getFirst()));
        assertEquals(postTitle, response.getTitle());
        assertEquals(postText, response.getText());
        fileService.updatePostFile(file, response.getId());
        Resource resource = fileService.findFileByPostId(response.getId());
        assertNotNull(resource);
        assertEquals("1.png", resource.getFilename());
        assertArrayEquals(file.getBytes(), resource.getContentAsByteArray());
    }
}

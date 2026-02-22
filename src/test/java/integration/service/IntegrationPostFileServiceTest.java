package integration.service;

import configuration.integration.service.IntegrationServiceConfiguration;
import integration.AbstractIntegrationTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ru.blog.backend.dto.post.PostCreateRequestDto;
import ru.blog.backend.dto.post.PostResponseDto;
import ru.blog.backend.service.PostFileService;
import ru.blog.backend.service.PostService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@ContextHierarchy(@ContextConfiguration(name = "service", classes = IntegrationServiceConfiguration.class))
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class IntegrationPostFileServiceTest extends AbstractIntegrationTest {

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
    public void shouldUpdatePostFileAndGet() throws IOException {
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

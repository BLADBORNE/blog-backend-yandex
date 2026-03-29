package ru.yandex.blog.integration.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.yandex.blog.configuration.integration.service.IntegrationServiceConfiguration;
import ru.yandex.blog.dto.post.PostCreateRequestDto;
import ru.yandex.blog.dto.post.PostPageResponseDto;
import ru.yandex.blog.dto.post.PostResponseDto;
import ru.yandex.blog.dto.post.PostUpdateRequestDto;
import ru.yandex.blog.integration.DynamicIntegrationTempDir;
import ru.yandex.blog.page.PageableRequest;
import ru.yandex.blog.service.PostService;
import ru.yandex.blog.integration.PostgreTestContainer;

import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

@SpringJUnitConfig(classes = IntegrationServiceConfiguration.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Testcontainers
@ImportTestcontainers(PostgreTestContainer.class)
@Sql(scripts = "classpath:/sql/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
@Sql(scripts = "classpath:/sql/schema.sql", executionPhase = BEFORE_TEST_CLASS)
class IntegrationPostServiceTest extends DynamicIntegrationTempDir {

    private final PostService postService;

    private final List<String> tags = List.of("JAVA", "python");

    private final String postTitle = "Tes1";

    private final String postText = "Text";

    private final PostCreateRequestDto requestDto = PostCreateRequestDto.builder()
        .title(postTitle)
        .text(postText)
        .tags(tags)
        .build();

    @Test
    void shouldSave() {
        PostResponseDto response = postService.save(requestDto);
        assertNotNull(response);
        assertTrue(response.getTags().contains(tags.getFirst()));
        assertEquals(postTitle, response.getTitle());
        assertEquals(postText, response.getText());
    }

    @Test
    void shouldUpdate() {
        PostResponseDto response = postService.save(requestDto);
        assertNotNull(response);
        assertTrue(response.getTags().contains(tags.getFirst()));
        assertEquals(postTitle, response.getTitle());
        assertEquals(postText, response.getText());
        PostUpdateRequestDto updateRequestDto = PostUpdateRequestDto.builder()
            .id(response.getId())
            .title("UPDATED")
            .text("TEXT NEW")
            .tags(List.of("NEW"))
            .build();
        PostResponseDto updatedResponse = postService.update(updateRequestDto);
        assertNotNull(updatedResponse);
        assertEquals("UPDATED", updatedResponse.getTitle());
        assertEquals("TEXT NEW", updatedResponse.getText());
        assertEquals(singletonList("NEW"), updatedResponse.getTags());
        response = postService.findById(response.getId());
        assertNotNull(response);
        assertEquals("UPDATED", response.getTitle());
        assertEquals("TEXT NEW", response.getText());
        assertEquals(singletonList("NEW"), response.getTags());
    }

    @Test
    void shouldIncreaseLike() {
        PostResponseDto response = postService.save(requestDto);
        assertNotNull(response);
        assertTrue(response.getTags().contains(tags.getFirst()));
        assertEquals(postTitle, response.getTitle());
        assertEquals(postText, response.getText());
        Integer like = postService.increaseLike(response.getId());
        assertNotNull(like);
        assertEquals(1, like);
        response = postService.findById(response.getId());
        assertNotNull(response);
        assertEquals(1, response.getLikesCount());
    }

    @Test
    void shouldFindById() {
        PostResponseDto response = postService.save(requestDto);
        assertNotNull(response);
        assertTrue(response.getTags().contains(tags.getFirst()));
        assertEquals(postTitle, response.getTitle());
        assertEquals(postText, response.getText());
        response = postService.findById(response.getId());
        assertNotNull(response);
        assertEquals(requestDto.getTitle(), response.getTitle());
        assertEquals(requestDto.getText(), response.getText());
        assertEquals(tags, response.getTags());
    }

    @Test
    void shouldExistsById() {
        PostResponseDto response = postService.save(requestDto);
        assertNotNull(response);
        assertTrue(response.getTags().contains(tags.getFirst()));
        assertEquals(postTitle, response.getTitle());
        assertEquals(postText, response.getText());
        Boolean exists = postService.existsById(response.getId());
        assertNotNull(exists);
        assertEquals(TRUE, exists);
    }

    @Test
    void shouldDeleteById() {
        PostResponseDto response = postService.save(requestDto);
        assertNotNull(response);
        assertTrue(response.getTags().contains(tags.getFirst()));
        assertEquals(postTitle, response.getTitle());
        assertEquals(postText, response.getText());
        postService.deleteById(response.getId());
        Boolean exists = postService.existsById(response.getId());
        assertNotNull(exists);
        assertEquals(FALSE, exists);
    }

    @Test
    void shouldFindPostsBySearchString() {
        PostResponseDto response = postService.save(requestDto);
        assertNotNull(response);
        assertTrue(response.getTags().contains(tags.getFirst()));
        assertEquals(postTitle, response.getTitle());
        assertEquals(postText, response.getText());
        PostPageResponseDto result = postService.findPostsBySearchString(
            "Tes1",
            new PageableRequest(1, 1)
        );
        assertNotNull(result);
        assertEquals(1, result.getPosts().size());
        PostResponseDto posts = result.getPosts().iterator().next();
        assertNotNull(posts);
        assertEquals(requestDto.getText(), posts.getText());
        assertEquals(requestDto.getTitle(), posts.getTitle());
        assertEquals(requestDto.getTags(), posts.getTags());
    }
}

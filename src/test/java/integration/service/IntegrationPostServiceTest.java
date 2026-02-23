package integration.service;

import configuration.integration.service.IntegrationServiceConfiguration;
import integration.AbstractIntegrationTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import ru.blog.backend.dto.post.PostCreateRequestDto;
import ru.blog.backend.dto.post.PostPageResponseDto;
import ru.blog.backend.dto.post.PostResponseDto;
import ru.blog.backend.dto.post.PostUpdateRequestDto;
import ru.blog.backend.page.PageableRequest;
import ru.blog.backend.service.PostService;

import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ContextHierarchy(@ContextConfiguration(name = "service", classes = IntegrationServiceConfiguration.class))
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class IntegrationPostServiceTest extends AbstractIntegrationTest {

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
    public void shouldSave() {
        PostResponseDto response = postService.save(requestDto);
        assertNotNull(response);
        assertTrue(response.getTags().contains(tags.getFirst()));
        assertEquals(postTitle, response.getTitle());
        assertEquals(postText, response.getText());
    }

    @Test
    public void shouldUpdate() {
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
    public void shouldIncreaseLike() {
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
    public void shouldFindById() {
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
    public void shouldExistsById() {
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
    public void shouldDeleteById() {
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
    public void shouldFindPostsBySearchString() {
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

package integration.service;

import configuration.integration.service.IntegrationServiceConfiguration;
import integration.AbstractIntegrationTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import ru.blog.backend.dto.post.PostCreateRequestDto;
import ru.blog.backend.dto.post.PostResponseDto;
import ru.blog.backend.dto.tag.PostTagDto;
import ru.blog.backend.dto.tag.PostTagInsertDto;
import ru.blog.backend.dto.tag.TagDto;
import ru.blog.backend.service.PostService;
import ru.blog.backend.service.TagService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ContextHierarchy(@ContextConfiguration(name = "service", classes = IntegrationServiceConfiguration.class))
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class IntegrationTagServiceTest extends AbstractIntegrationTest {

    private final TagService tagService;

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
    public void shouldFindById() {
        PostResponseDto response = postService.save(requestDto);
        assertNotNull(response);
        assertTrue(response.getTags().contains(tags.getFirst()));
        assertEquals(postTitle, response.getTitle());
        assertEquals(postText, response.getText());
        List<TagDto> tagsResult = tagService.findByPostId(response.getId());
        assertNotNull(tags);
        assertEquals(2, tags.size());
        assertEquals(2, tagsResult.stream().map(TagDto::title).filter(tags::contains).toList().size());
    }

    @Test
    public void shouldSave() {
        List<TagDto> response = tagService.save(tags);
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(2, response.stream().map(TagDto::title).filter(tags::contains).toList().size());
    }

    @Test
    public void shouldSaveTagsToPost() {
        PostResponseDto responsePost = postService.save(requestDto);
        assertNotNull(responsePost);
        assertTrue(responsePost.getTags().contains(tags.getFirst()));
        assertEquals(postTitle, responsePost.getTitle());
        assertEquals(postText, responsePost.getText());
        List<TagDto> responseTags = tagService.save(tags);
        assertNotNull(responseTags);
        assertEquals(2, responseTags.size());
        assertEquals(2, responseTags.stream().map(TagDto::title).filter(tags::contains).toList().size());
        tagService.saveTagsToPost(
            new PostTagInsertDto(responsePost.getId(), responseTags.stream().map(TagDto::id).toList())
        );
        List<TagDto> responsePostTags = tagService.findByPostId(responsePost.getId());
        assertNotNull(responsePostTags);
        assertEquals(2, responsePostTags.size());
        assertEquals(2, responsePostTags.stream().map(TagDto::title).filter(tags::contains).toList().size());
    }

    @Test
    public void shouldFindTagsByPostIdIn() {
        PostResponseDto responsePost = postService.save(requestDto);
        assertNotNull(responsePost);
        assertTrue(responsePost.getTags().contains(tags.getFirst()));
        assertEquals(postTitle, responsePost.getTitle());
        assertEquals(postText, responsePost.getText());
        List<TagDto> responseTags = tagService.save(tags);
        assertNotNull(responseTags);
        assertEquals(2, responseTags.size());
        assertEquals(2, responseTags.stream().map(TagDto::title).filter(tags::contains).toList().size());
        tagService.saveTagsToPost(
            new PostTagInsertDto(responsePost.getId(), responseTags.stream().map(TagDto::id).toList())
        );
        List<TagDto> responsePostTags = tagService.findByPostId(responsePost.getId());
        assertNotNull(responsePostTags);
        assertEquals(2, responsePostTags.size());
        assertEquals(2, responsePostTags.stream().map(TagDto::title).filter(tags::contains).toList().size());
        Map<Long, List<PostTagDto>> postTags = tagService.findTagsByPostIdIn(List.of(responsePost.getId()));
        assertNotNull(postTags);
        List<PostTagDto> result = postTags.get(responsePost.getId());
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void shouldDeleteTagsByPostId() {
        PostResponseDto responsePost = postService.save(requestDto);
        assertNotNull(responsePost);
        assertTrue(responsePost.getTags().contains(tags.getFirst()));
        assertEquals(postTitle, responsePost.getTitle());
        assertEquals(postText, responsePost.getText());
        List<TagDto> responseTags = tagService.save(tags);
        assertNotNull(responseTags);
        assertEquals(2, responseTags.size());
        assertEquals(2, responseTags.stream().map(TagDto::title).filter(tags::contains).toList().size());
        tagService.saveTagsToPost(
            new PostTagInsertDto(responsePost.getId(), responseTags.stream().map(TagDto::id).toList())
        );
        List<TagDto> responsePostTags = tagService.findByPostId(responsePost.getId());
        assertNotNull(responsePostTags);
        assertEquals(2, responsePostTags.size());
        assertEquals(2, responsePostTags.stream().map(TagDto::title).filter(tags::contains).toList().size());
        tagService.deleteTagsByPostId(responsePost.getId());
        responsePostTags = tagService.findByPostId(responsePost.getId());
        assertTrue(responsePostTags.isEmpty());
    }
}

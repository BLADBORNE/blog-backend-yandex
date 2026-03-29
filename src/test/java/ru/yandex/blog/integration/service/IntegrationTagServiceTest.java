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
import ru.yandex.blog.dto.post.PostResponseDto;
import ru.yandex.blog.dto.tag.PostTagDto;
import ru.yandex.blog.dto.tag.PostTagInsertDto;
import ru.yandex.blog.dto.tag.TagDto;
import ru.yandex.blog.integration.DynamicIntegrationTempDir;
import ru.yandex.blog.service.PostService;
import ru.yandex.blog.service.TagService;
import ru.yandex.blog.integration.PostgreTestContainer;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

@SpringJUnitConfig(classes = IntegrationServiceConfiguration.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Testcontainers
@ImportTestcontainers(PostgreTestContainer.class)
@Sql(scripts = "classpath:/sql/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
@Sql(scripts = "classpath:/sql/schema.sql", executionPhase = BEFORE_TEST_CLASS)
class IntegrationTagServiceTest extends DynamicIntegrationTempDir {

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
    void shouldFindById() {
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
    void shouldSave() {
        List<TagDto> response = tagService.save(tags);
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(2, response.stream().map(TagDto::title).filter(tags::contains).toList().size());
    }

    @Test
    void shouldSaveTagsToPost() {
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
    void shouldFindTagsByPostIdIn() {
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
    void shouldDeleteTagsByPostId() {
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

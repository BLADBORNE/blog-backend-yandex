package ru.yandex.blog.integration.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.yandex.blog.configuration.integration.service.IntegrationServiceConfiguration;
import ru.yandex.blog.dto.comment.CommentCreateRequestDto;
import ru.yandex.blog.dto.comment.CommentDto;
import ru.yandex.blog.dto.post.PostCreateRequestDto;
import ru.yandex.blog.dto.post.PostResponseDto;
import ru.yandex.blog.integration.DynamicIntegrationTempDir;
import ru.yandex.blog.integration.PostgreTestContainer;
import ru.yandex.blog.service.CommentService;
import ru.yandex.blog.service.PostService;

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
class IntegrationCommentServiceTest extends DynamicIntegrationTempDir {

    private final CommentService commentService;

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
    void shouldAddCommentToPost() {
        PostResponseDto response = postService.save(requestDto);
        assertNotNull(response);
        assertTrue(response.getTags().contains(tags.getFirst()));
        assertEquals(postTitle, response.getTitle());
        assertEquals(postText, response.getText());
        CommentCreateRequestDto requestDto = CommentCreateRequestDto.builder().text("c").postId(response.getId()).build();
        CommentDto result = commentService.addCommentToPost(requestDto);
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(response.getId(), result.getPostId());
        assertEquals(requestDto.getText(), result.getText());
    }

    @Test
    void shouldFindCommentsByPostId() {
        PostResponseDto response = postService.save(requestDto);
        assertNotNull(response);
        assertTrue(response.getTags().contains(tags.getFirst()));
        assertEquals(postTitle, response.getTitle());
        assertEquals(postText, response.getText());
        CommentCreateRequestDto requestDto = CommentCreateRequestDto.builder().text("c").postId(response.getId()).build();
        CommentDto result = commentService.addCommentToPost(requestDto);
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(response.getId(), result.getPostId());
        assertEquals(requestDto.getText(), result.getText());
        List<CommentDto> comments = commentService.findCommentsByPostId(response.getId());
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals(requestDto.getText(), comments.getFirst().getText());
    }

    @Test
    void shouldUpdatePostComment() {
        PostResponseDto response = postService.save(requestDto);
        assertNotNull(response);
        assertTrue(response.getTags().contains(tags.getFirst()));
        assertEquals(postTitle, response.getTitle());
        assertEquals(postText, response.getText());
        CommentCreateRequestDto requestDto = CommentCreateRequestDto.builder().text("c").postId(response.getId()).build();
        CommentDto result = commentService.addCommentToPost(requestDto);
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(response.getId(), result.getPostId());
        assertEquals(requestDto.getText(), result.getText());
        result = commentService.updatePostComment(CommentDto.builder().id(result.getId()).postId(response.getId()).text("NEW").build());
        assertNotNull(result);
        assertEquals("NEW", result.getText());
        List<CommentDto> comments = commentService.findCommentsByPostId(response.getId());
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals("NEW", comments.getFirst().getText());
    }

    @Test
    void shouldCountCommentsByPostId() {
        PostResponseDto response = postService.save(requestDto);
        assertNotNull(response);
        assertTrue(response.getTags().contains(tags.getFirst()));
        assertEquals(postTitle, response.getTitle());
        assertEquals(postText, response.getText());
        CommentCreateRequestDto requestDto = CommentCreateRequestDto.builder().text("c").postId(response.getId()).build();
        CommentDto result = commentService.addCommentToPost(requestDto);
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(response.getId(), result.getPostId());
        assertEquals(requestDto.getText(), result.getText());
        Long count = commentService.countCommentsByPostId(response.getId());
        assertNotNull(count);
        assertEquals(1, count);
        List<CommentDto> comments = commentService.findCommentsByPostId(response.getId());
        assertNotNull(comments);
        assertEquals(1, comments.size());
    }

    @Test
    void shouldCountCommentsByPostIds() {
        PostResponseDto response = postService.save(requestDto);
        assertNotNull(response);
        assertTrue(response.getTags().contains(tags.getFirst()));
        assertEquals(postTitle, response.getTitle());
        assertEquals(postText, response.getText());
        CommentCreateRequestDto requestDto = CommentCreateRequestDto.builder().text("c").postId(response.getId()).build();
        CommentDto result = commentService.addCommentToPost(requestDto);
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(response.getId(), result.getPostId());
        assertEquals(requestDto.getText(), result.getText());
        Map<Long, Long> count = commentService.countCommentsByPostIds(List.of(response.getId()));
        assertNotNull(count);
        assertEquals(1, count.get(response.getId()));
        List<CommentDto> comments = commentService.findCommentsByPostId(response.getId());
        assertNotNull(comments);
        assertEquals(1, comments.size());
    }

    @Test
    void shouldFindCommentByPostIdAndCommentId() {
        PostResponseDto response = postService.save(requestDto);
        assertNotNull(response);
        assertTrue(response.getTags().contains(tags.getFirst()));
        assertEquals(postTitle, response.getTitle());
        assertEquals(postText, response.getText());
        CommentCreateRequestDto requestDto = CommentCreateRequestDto.builder().text("c").postId(response.getId()).build();
        CommentDto result = commentService.addCommentToPost(requestDto);
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(response.getId(), result.getPostId());
        assertEquals(requestDto.getText(), result.getText());
        CommentDto commentDto = commentService.findCommentByPostIdAndCommentId(response.getId(), result.getId());
        assertNotNull(commentDto);
        assertEquals(response.getId(), commentDto.getPostId());
        assertEquals("c", commentDto.getText());
        assertEquals(result.getId(), commentDto.getId());
        List<CommentDto> comments = commentService.findCommentsByPostId(response.getId());
        assertNotNull(comments);
        assertEquals(1, comments.size());
    }

    @Test
    void shouldDeleteById() {
        PostResponseDto response = postService.save(requestDto);
        assertNotNull(response);
        assertTrue(response.getTags().contains(tags.getFirst()));
        assertEquals(postTitle, response.getTitle());
        assertEquals(postText, response.getText());
        CommentCreateRequestDto requestDto = CommentCreateRequestDto.builder().text("c").postId(response.getId()).build();
        CommentDto result = commentService.addCommentToPost(requestDto);
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(response.getId(), result.getPostId());
        assertEquals(requestDto.getText(), result.getText());
        commentService.deleteById(result.getId());
        List<CommentDto> comments = commentService.findCommentsByPostId(response.getId());
        assertNotNull(comments);
        assertEquals(0, comments.size());
    }
}

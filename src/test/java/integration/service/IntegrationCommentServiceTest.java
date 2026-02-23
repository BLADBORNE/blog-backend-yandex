package integration.service;

import configuration.integration.service.IntegrationServiceConfiguration;
import integration.AbstractIntegrationTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import ru.blog.backend.dto.comment.CommentCreateRequestDto;
import ru.blog.backend.dto.comment.CommentDto;
import ru.blog.backend.dto.post.PostCreateRequestDto;
import ru.blog.backend.dto.post.PostResponseDto;
import ru.blog.backend.service.CommentService;
import ru.blog.backend.service.PostService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ContextHierarchy(@ContextConfiguration(name = "service", classes = IntegrationServiceConfiguration.class))
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class IntegrationCommentServiceTest extends AbstractIntegrationTest {

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
    public void shouldAddCommentToPost() {
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
    public void shouldFindCommentsByPostId() {
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
    public void shouldUpdatePostComment() {
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
    public void shouldCountCommentsByPostId() {
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
    public void shouldCountCommentsByPostIds() {
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
    public void shouldFindCommentByPostIdAndCommentId() {
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
    public void shouldDeleteById() {
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

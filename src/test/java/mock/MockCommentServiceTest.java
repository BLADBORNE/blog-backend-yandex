package mock;

import configuration.mock.MockCommentServiceConfiguration;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.blog.backend.dto.comment.CommentCreateRequestDto;
import ru.blog.backend.dto.comment.CommentDto;
import ru.blog.backend.repository.CommentRepository;
import ru.blog.backend.repository.PostRepository;
import ru.blog.backend.service.CommentService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Long.MAX_VALUE;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.reset;

@SpringJUnitConfig(classes = {MockCommentServiceConfiguration.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MockCommentServiceTest {

    private final CommentService commentService;

    private final CommentRepository commentRepository;

    private final PostRepository postRepository;

    private final Long postId = ThreadLocalRandom.current().nextLong(1, MAX_VALUE);

    private final Long commentId = ThreadLocalRandom.current().nextLong(1, MAX_VALUE);

    private final String text = "SomeText";

    private final CommentDto commentDto = CommentDto.builder().id(commentId).postId(postId).text(text).build();

    private final ArgumentCaptor<Long> captorPostId = ArgumentCaptor.forClass(Long.class);

    private final ArgumentCaptor<CommentCreateRequestDto> captorRequestDto = ArgumentCaptor.forClass(CommentCreateRequestDto.class);

    private final ArgumentCaptor<Long> captorCommentId = ArgumentCaptor.forClass(Long.class);

    @BeforeEach
    public void resetMocks() {
        reset(postRepository, commentRepository);
    }

    @Nested
    class WhenAddCommentToPost {
        @Test
        public void shouldAddCommentToPostWhenPostExists() {
            CommentCreateRequestDto requestDto = CommentCreateRequestDto.builder().text(text).postId(postId).build();
            when(postRepository.existsById(postId)).thenReturn(TRUE);
            when(commentRepository.addCommentToPost(requestDto)).thenReturn(commentDto);
            CommentDto result = commentService.addCommentToPost(requestDto);
            assertNotNull(result);
            assertEquals(postId, result.getPostId());
            assertEquals(text, result.getText());
            verify(postRepository, times(1)).existsById(captorPostId.capture());
            verify(commentRepository, times(1)).addCommentToPost(captorRequestDto.capture());
            assertEquals(postId, captorPostId.getValue());
            assertEquals(text, captorRequestDto.getValue().getText());
        }

        @Test
        public void shouldAddCommentToPostWhenPostNotExists() {
            CommentCreateRequestDto requestDto = CommentCreateRequestDto.builder().text(text).postId(postId).build();
            when(postRepository.existsById(postId)).thenReturn(FALSE);
            assertThrows(NoSuchElementException.class, () -> commentService.addCommentToPost(requestDto));
            verify(commentRepository, times(0)).addCommentToPost(captorRequestDto.capture());
        }

    }

    @Nested
    class WhenFindCommentByPostIdAndCommentId {
        @Test
        public void shouldFindCommentByPostIdAndCommentIdWhenPostExists() {
            when(postRepository.existsById(postId)).thenReturn(TRUE);
            when(commentRepository.findCommentById(commentId)).thenReturn(Optional.of(commentDto));
            CommentDto result = commentService.findCommentByPostIdAndCommentId(postId, commentId);
            verify(postRepository, times(1)).existsById(captorPostId.capture());
            verify(commentRepository, times(1)).findCommentById(captorCommentId.capture());
            assertNotNull(result);
            assertEquals(postId, result.getPostId());
            assertEquals(text, result.getText());
            assertEquals(postId, captorPostId.getValue());
            assertEquals(commentId, captorCommentId.getValue());
        }

        @Test
        public void shouldFindCommentByPostIdAndCommentIdWhenPostNotExists() {
            when(postRepository.existsById(postId)).thenReturn(FALSE);
            assertThrows(NoSuchElementException.class, () -> commentService.findCommentByPostIdAndCommentId(postId, commentId));
            verify(commentRepository, times(0)).findCommentById(captorCommentId.capture());
        }

        @Test
        public void shouldFindCommentByPostIdAndCommentIdWhenPostExistsAndCommentNotExists() {
            when(postRepository.existsById(postId)).thenReturn(TRUE);
            when(commentRepository.findCommentById(commentId)).thenReturn(Optional.empty());
            assertThrows(NoSuchElementException.class, () -> commentService.findCommentByPostIdAndCommentId(postId, commentId));
        }
    }

    @Nested
    class WhenDeleteById {
        @Test
        public void shouldDeleteByIdWhenExists() {
            when(commentRepository.existsById(commentId)).thenReturn(TRUE);
            commentService.deleteById(commentId);
            verify(commentRepository, times(1)).existsById(captorCommentId.capture());
            verify(commentRepository, times(1)).deleteById(captorCommentId.capture());
            assertEquals(commentId, captorCommentId.getValue());
        }

        @Test
        public void shouldDeleteByIdWhenNotExists() {
            when(commentRepository.existsById(commentId)).thenReturn(FALSE);
            assertThrows(NoSuchElementException.class, () -> commentService.deleteById(commentId));
            verify(commentRepository, times(0)).deleteById(captorCommentId.capture());
        }
    }

    @Nested
    class WhenFindCommentsByPostId {
        @Test
        public void shouldFindCommentsByPostIdWhenPostExists() {
            when(postRepository.existsById(postId)).thenReturn(TRUE);
            when(commentRepository.findCommentsByPostId(postId)).thenReturn(singletonList(commentDto));
            List<CommentDto> result = commentService.findCommentsByPostId(postId);
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(text, result.getFirst().getText());
            assertEquals(postId, result.getFirst().getPostId());
            verify(postRepository, times(1)).existsById(captorPostId.capture());
            verify(commentRepository, times(1)).findCommentsByPostId(captorPostId.capture());
            assertEquals(postId, captorPostId.getValue());
        }

        @Test
        public void shouldFindCommentsByPostIdWhenPostNotExists() {
            when(postRepository.existsById(postId)).thenReturn(FALSE);
            assertThrows(NoSuchElementException.class, () -> commentService.findCommentsByPostId(postId));
            verify(postRepository, times(1)).existsById(captorPostId.capture());
            verify(commentRepository, times(0)).findCommentsByPostId(captorPostId.capture());
        }
    }

    @Nested
    class WhenUpdatePostComment {
        @Test
        public void shouldUpdatePostCommentWhenCommentExists() {
            when(commentRepository.existsById(commentId)).thenReturn(TRUE);
            when(commentRepository.updatePostComment(commentDto)).thenReturn(commentDto);
            CommentDto result = commentService.updatePostComment(commentDto);
            assertNotNull(result);
            assertEquals(text, result.getText());
            assertEquals(commentId, result.getId());
            verify(commentRepository, times(1)).existsById(captorCommentId.capture());
            ArgumentCaptor<CommentDto> captor = ArgumentCaptor.forClass(CommentDto.class);
            verify(commentRepository, times(1)).updatePostComment(captor.capture());
            assertEquals(commentId, captorCommentId.getValue());
            assertEquals(text, captor.getValue().getText());
        }

        @Test
        public void shouldUpdatePostCommentWhenCommentNotExists() {
            when(commentRepository.existsById(commentId)).thenReturn(FALSE);
            assertThrows(NoSuchElementException.class, () -> commentService.updatePostComment(commentDto));
            verify(commentRepository, times(1)).existsById(captorCommentId.capture());
            ArgumentCaptor<CommentDto> captor = ArgumentCaptor.forClass(CommentDto.class);
            verify(commentRepository, times(0)).updatePostComment(captor.capture());
            assertEquals(commentId, captorCommentId.getValue());
        }
    }
}

package ru.yandex.blog.mock;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.blog.dto.comment.CommentCreateRequestDto;
import ru.yandex.blog.dto.comment.CommentDto;
import ru.yandex.blog.repository.CommentRepository;
import ru.yandex.blog.service.impl.CommentServiceImpl;
import ru.yandex.blog.service.impl.PostExistenceChecker;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Long.MAX_VALUE;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MockCommentServiceTest {

    @InjectMocks
    private CommentServiceImpl commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostExistenceChecker postExistenceChecker;

    private final Long postId = ThreadLocalRandom.current().nextLong(1, MAX_VALUE);

    private final Long commentId = ThreadLocalRandom.current().nextLong(1, MAX_VALUE);

    private final String text = "SomeText";

    private final CommentDto commentDto = CommentDto.builder().id(commentId).postId(postId).text(text).build();

    private final ArgumentCaptor<Long> captorPostId = ArgumentCaptor.forClass(Long.class);

    private final ArgumentCaptor<CommentCreateRequestDto> captorRequestDto = ArgumentCaptor.forClass(CommentCreateRequestDto.class);

    private final ArgumentCaptor<Long> captorCommentId = ArgumentCaptor.forClass(Long.class);

    @Nested
    class WhenAddCommentToPost {
        @Test
        void shouldAddCommentToPostWhenPostExists() {
            CommentCreateRequestDto requestDto = CommentCreateRequestDto.builder().text(text).postId(postId).build();
            doNothing().when(postExistenceChecker).requirePost(postId);
            when(commentRepository.addCommentToPost(requestDto)).thenReturn(commentDto);
            CommentDto result = commentService.addCommentToPost(requestDto);
            assertNotNull(result);
            assertEquals(postId, result.getPostId());
            assertEquals(text, result.getText());
            verify(postExistenceChecker, times(1)).requirePost(captorPostId.capture());
            verify(commentRepository, times(1)).addCommentToPost(captorRequestDto.capture());
            assertEquals(postId, captorPostId.getValue());
            assertEquals(text, captorRequestDto.getValue().getText());
        }

        @Test
        void shouldAddCommentToPostWhenPostNotExists() {
            CommentCreateRequestDto requestDto = CommentCreateRequestDto.builder().text(text).postId(postId).build();
            doThrow(NoSuchElementException.class).when(postExistenceChecker).requirePost(postId);
            assertThrows(NoSuchElementException.class, () -> commentService.addCommentToPost(requestDto));
            verify(commentRepository, times(0)).addCommentToPost(captorRequestDto.capture());
        }
    }

    @Nested
    class WhenFindCommentByPostIdAndCommentId {
        @Test
        void shouldFindCommentByPostIdAndCommentIdWhenPostExists() {
            doNothing().when(postExistenceChecker).requirePost(postId);
            when(commentRepository.findCommentById(commentId)).thenReturn(Optional.of(commentDto));
            CommentDto result = commentService.findCommentByPostIdAndCommentId(postId, commentId);
            verify(postExistenceChecker, times(1)).requirePost(captorPostId.capture());
            verify(commentRepository, times(1)).findCommentById(captorCommentId.capture());
            assertNotNull(result);
            assertEquals(postId, result.getPostId());
            assertEquals(text, result.getText());
            assertEquals(postId, captorPostId.getValue());
            assertEquals(commentId, captorCommentId.getValue());
        }

        @Test
        void shouldFindCommentByPostIdAndCommentIdWhenPostNotExists() {
            doThrow(NoSuchElementException.class).when(postExistenceChecker).requirePost(postId);
            assertThrows(NoSuchElementException.class, () -> commentService.findCommentByPostIdAndCommentId(postId, commentId));
            verify(commentRepository, times(0)).findCommentById(captorCommentId.capture());
        }

        @Test
        void shouldFindCommentByPostIdAndCommentIdWhenPostExistsAndCommentNotExists() {
            doNothing().when(postExistenceChecker).requirePost(postId);
            when(commentRepository.findCommentById(commentId)).thenReturn(Optional.empty());
            assertThrows(NoSuchElementException.class, () -> commentService.findCommentByPostIdAndCommentId(postId, commentId));
        }
    }

    @Nested
    class WhenDeleteById {
        @Test
        void shouldDeleteByIdWhenExists() {
            when(commentRepository.existsById(commentId)).thenReturn(TRUE);
            commentService.deleteById(commentId);
            verify(commentRepository, times(1)).existsById(captorCommentId.capture());
            verify(commentRepository, times(1)).deleteById(captorCommentId.capture());
            assertEquals(commentId, captorCommentId.getValue());
        }

        @Test
        void shouldDeleteByIdWhenNotExists() {
            when(commentRepository.existsById(commentId)).thenReturn(FALSE);
            assertThrows(NoSuchElementException.class, () -> commentService.deleteById(commentId));
            verify(commentRepository, times(0)).deleteById(captorCommentId.capture());
        }
    }

    @Nested
    class WhenFindCommentsByPostId {
        @Test
        void shouldFindCommentsByPostIdWhenPostExists() {
            doNothing().when(postExistenceChecker).requirePost(postId);
            when(commentRepository.findCommentsByPostId(postId)).thenReturn(singletonList(commentDto));
            List<CommentDto> result = commentService.findCommentsByPostId(postId);
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(text, result.getFirst().getText());
            assertEquals(postId, result.getFirst().getPostId());
            verify(postExistenceChecker, times(1)).requirePost(captorPostId.capture());
            verify(commentRepository, times(1)).findCommentsByPostId(captorPostId.capture());
            assertEquals(postId, captorPostId.getValue());
        }

        @Test
        void shouldFindCommentsByPostIdWhenPostNotExists() {
            doThrow(NoSuchElementException.class).when(postExistenceChecker).requirePost(postId);
            assertThrows(NoSuchElementException.class, () -> commentService.findCommentsByPostId(postId));
            verify(postExistenceChecker, times(1)).requirePost(captorPostId.capture());
            verify(commentRepository, times(0)).findCommentsByPostId(captorPostId.capture());
        }
    }

    @Nested
    class WhenUpdatePostComment {
        @Test
        void shouldUpdatePostCommentWhenCommentExists() {
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
        void shouldUpdatePostCommentWhenCommentNotExists() {
            when(commentRepository.existsById(commentId)).thenReturn(FALSE);
            assertThrows(NoSuchElementException.class, () -> commentService.updatePostComment(commentDto));
            verify(commentRepository, times(1)).existsById(captorCommentId.capture());
            ArgumentCaptor<CommentDto> captor = ArgumentCaptor.forClass(CommentDto.class);
            verify(commentRepository, times(0)).updatePostComment(captor.capture());
            assertEquals(commentId, captorCommentId.getValue());
        }
    }
}

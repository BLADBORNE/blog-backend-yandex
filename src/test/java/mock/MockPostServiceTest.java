package mock;

import configuration.mock.MockPostServiceConfiguration;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.blog.backend.dto.post.PostCreateRequestDto;
import ru.blog.backend.dto.post.PostPageResponseDto;
import ru.blog.backend.dto.post.PostResponseDto;
import ru.blog.backend.dto.post.PostUpdateRequestDto;
import ru.blog.backend.dto.tag.PostTagDto;
import ru.blog.backend.dto.tag.PostTagInsertDto;
import ru.blog.backend.dto.tag.TagDto;
import ru.blog.backend.page.PageableRequest;
import ru.blog.backend.repository.CommentRepository;
import ru.blog.backend.repository.PostRepository;
import ru.blog.backend.repository.TagRepository;
import ru.blog.backend.service.PostService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Long.MAX_VALUE;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doNothing;
import static ru.blog.backend.model.SqlOperation.SELECT_COUNT;
import static ru.blog.backend.model.SqlOperation.SELECT_DATA;

@SpringJUnitConfig(classes = MockPostServiceConfiguration.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MockPostServiceTest {

    private final PostService postService;

    private final PostRepository postRepository;

    private final TagRepository tagRepository;

    private final CommentRepository commentRepository;

    private final List<String> tags = List.of("JAVA", "python");

    private final String postTitle = "Tes1";

    private final String postText = "Text";

    private final Long postId = ThreadLocalRandom.current().nextLong(1, MAX_VALUE);

    private final PostResponseDto responseDto = PostResponseDto.builder()
        .id(postId)
        .likesCount(1L)
        .commentsCount(1L)
        .title(postTitle)
        .text(postText)
        .tags(tags)
        .build();

    private final Long tagId = ThreadLocalRandom.current().nextLong(1, MAX_VALUE);

    private final TagDto tagDto = new TagDto(tagId, tags.getFirst());

    private final PostTagInsertDto insertDto = new PostTagInsertDto(postId, singletonList(tagId));

    private final PostUpdateRequestDto updateRequestDto = PostUpdateRequestDto.builder()
        .id(postId)
        .title(postTitle)
        .text(postText)
        .tags(tags)
        .build();

    private final String notValidString = "   ";

    private final PageableRequest pageRequest = new PageableRequest(1, 1);

    @BeforeEach
    public void resetMocks() {
        reset(postRepository, tagRepository, commentRepository);
    }

    @Test
    public void shouldSavePost() {
        PostCreateRequestDto requestDto = PostCreateRequestDto.builder().title(postTitle).text(postText).tags(tags).build();
        when(postRepository.save(requestDto)).thenReturn(responseDto);
        when(tagRepository.save(tags)).thenReturn(singletonList(tagDto));
        doNothing().when(tagRepository).saveTagsToPost(insertDto);
        PostResponseDto response = postService.save(requestDto);
        assertNotNull(response);
        assertEquals(1L, response.getCommentsCount());
        assertEquals(tags, response.getTags());
        assertEquals(postTitle, response.getTitle());
        ArgumentCaptor<PostCreateRequestDto> createPostCaptor = ArgumentCaptor.forClass(PostCreateRequestDto.class);
        verify(postRepository, times(1)).save(createPostCaptor.capture());
        assertEquals(postTitle, createPostCaptor.getValue().getTitle());
        assertEquals(postText, createPostCaptor.getValue().getText());
        verify(tagRepository, times(1)).save(tags);
        ArgumentCaptor<PostTagInsertDto> insertCaptorDto = ArgumentCaptor.forClass(PostTagInsertDto.class);
        verify(tagRepository, times(1)).saveTagsToPost(insertCaptorDto.capture());
        assertEquals(postId, insertCaptorDto.getValue().postId());
        assertEquals(tags.getLast(), tags.getLast());
    }

    @Nested
    class WhenUpdatePost {
        @Test
        public void shouldUpdateWhenPostExists() {
            when(postRepository.existsById(postId)).thenReturn(TRUE);
            when(postRepository.update(updateRequestDto)).thenReturn(responseDto);
            doNothing().when(tagRepository).deleteTagsByPostId(postId);
            when(tagRepository.save(tags)).thenReturn(singletonList(tagDto));
            doNothing().when(tagRepository).saveTagsToPost(insertDto);
            when(commentRepository.countCommentsByPostId(postId)).thenReturn(1L);
            PostResponseDto response = postService.update(updateRequestDto);
            assertNotNull(response);
            assertEquals(1L, response.getCommentsCount());
            assertEquals(tags, response.getTags());
            assertEquals(postTitle, response.getTitle());
            verify(postRepository, times(1)).existsById(postId);
            ArgumentCaptor<PostUpdateRequestDto> updatePostCaptor = ArgumentCaptor.forClass(PostUpdateRequestDto.class);
            verify(postRepository, times(1)).update(updatePostCaptor.capture());
            assertEquals(postTitle, updatePostCaptor.getValue().getTitle());
            assertEquals(postText, updatePostCaptor.getValue().getText());
            verify(tagRepository, times(1)).deleteTagsByPostId(postId);
            verify(tagRepository, times(1)).save(tags);
            ArgumentCaptor<PostTagInsertDto> insertCaptorDto = ArgumentCaptor.forClass(PostTagInsertDto.class);
            verify(tagRepository, times(1)).saveTagsToPost(insertCaptorDto.capture());
            assertEquals(postId, insertCaptorDto.getValue().postId());
            assertEquals(tags.getLast(), tags.getLast());
            verify(commentRepository, times(1)).countCommentsByPostId(postId);
        }

        @Test
        public void shouldUpdateWhenPostNotExists() {
            when(postRepository.existsById(postId)).thenReturn(FALSE);
            assertThrows(NoSuchElementException.class, () -> postService.update(updateRequestDto));
            verify(postRepository, times(1)).existsById(postId);
            ArgumentCaptor<PostUpdateRequestDto> updatePostCaptor = ArgumentCaptor.forClass(PostUpdateRequestDto.class);
            verify(postRepository, times(0)).update(updatePostCaptor.capture());
            verify(tagRepository, times(0)).deleteTagsByPostId(postId);
            verify(tagRepository, times(0)).save(tags);
            ArgumentCaptor<PostTagInsertDto> insertCaptorDto = ArgumentCaptor.forClass(PostTagInsertDto.class);
            verify(tagRepository, times(0)).saveTagsToPost(insertCaptorDto.capture());
            verify(commentRepository, times(0)).countCommentsByPostId(postId);
        }
    }

    @Nested
    class WhenIncreaseLike {
        @Test
        public void shouldIncreaseLikeWhenPostExists() {
            when(postRepository.existsById(postId)).thenReturn(TRUE);
            when(postRepository.increaseLike(postId)).thenReturn(1);
            Integer response = postService.increaseLike(postId);
            assertEquals(1, response);
            verify(postRepository, times(1)).existsById(postId);
            verify(postRepository, times(1)).increaseLike(postId);
        }

        @Test
        public void shouldIncreaseLikeWhenPostNExists() {
            when(postRepository.existsById(postId)).thenReturn(FALSE);
            assertThrows(NoSuchElementException.class, () -> postService.increaseLike(postId));
            verify(postRepository, times(1)).existsById(postId);
            verify(postRepository, times(0)).increaseLike(postId);
        }
    }

    @Nested
    class WhenFindPostById {
        @Test
        public void shouldFindPostByIdWhenExists() {
            when(postRepository.findById(postId)).thenReturn(Optional.of(responseDto));
            when(tagRepository.findByPostId(postId)).thenReturn(singletonList(tagDto));
            when(commentRepository.countCommentsByPostId(postId)).thenReturn(2L);
            PostResponseDto response = postService.findById(postId);
            assertNotNull(response);
            assertEquals(postTitle, response.getTitle());
            assertEquals(postText, response.getText());
            assertEquals(tags.getLast(), tags.getLast());
            assertEquals(2L, response.getCommentsCount());
            verify(postRepository, times(1)).findById(postId);
            verify(tagRepository, times(1)).findByPostId(postId);
            verify(commentRepository, times(1)).countCommentsByPostId(postId);
        }

        @Test
        public void shouldFindPostByIdWhenNotExists() {
            when(postRepository.findById(postId)).thenReturn(Optional.empty());
            assertThrows(NoSuchElementException.class, () -> postService.findById(postId));
            verify(postRepository, times(1)).findById(postId);
            verify(tagRepository, times(0)).findByPostId(postId);
            verify(commentRepository, times(0)).countCommentsByPostId(postId);
        }
    }

    @Nested
    class WhenExistsById {
        @Test
        public void shouldExistsById() {
            when(postRepository.existsById(postId)).thenReturn(TRUE);
            Boolean result = postService.existsById(postId);
            assertNotNull(result);
            assertEquals(TRUE, result);
            verify(postRepository, times(1)).existsById(postId);
        }

        @Test
        public void shouldNotExistsById() {
            when(postRepository.existsById(postId)).thenReturn(FALSE);
            Boolean result = postService.existsById(postId);
            assertNotNull(result);
            assertEquals(FALSE, result);
            verify(postRepository, times(1)).existsById(postId);
        }
    }

    @Nested
    class WhenDeleteById {
        @Test
        public void shouldDeleteByIdWhenPostExists() {
            when(postRepository.existsById(postId)).thenReturn(TRUE);
            doNothing().when(postRepository).deleteById(postId);
            postService.deleteById(postId);
            verify(postRepository, times(1)).existsById(postId);
            verify(postRepository, times(1)).deleteById(postId);
        }

        @Test
        public void shouldDeleteByIdWhenPostNotExists() {
            when(postRepository.existsById(postId)).thenReturn(FALSE);
            assertThrows(NoSuchElementException.class, () -> postService.deleteById(postId));
            verify(postRepository, times(1)).existsById(postId);
            verify(postRepository, times(0)).deleteById(postId);
        }
    }

    @Nested
    class WhenShouldFindPostsBySearchString {
        @Test
        public void shouldFindPostsBySearchStringWhenTagsAndWordsAreEmpty() {
            PostPageResponseDto response = postService.findPostsBySearchString(notValidString, pageRequest);
            assertNotNull(response);
            assertNotNull(response);
            assertTrue(response.getPosts().isEmpty());
            assertEquals(FALSE, response.getHasPrev());
            assertEquals(FALSE, response.getHasNext());
            assertEquals(0, response.getLastPage());
            verify(postRepository, times(0)).findTotalBySearch(eq(SELECT_COUNT), any(), any());
            verify(postRepository, times(0)).findBySearch(eq(SELECT_DATA), any(), any(), any());
            verify(tagRepository, times(0)).findTagsByPostIdIn(any());
            verify(commentRepository, times(0)).countCommentsByPostIds(any());
        }

        @Test
        public void shouldFindPostsBySearchStringWhenPageGreaterThanResult() {
            when(postRepository.findTotalBySearch(
                    SELECT_COUNT,
                    new String[]{},
                    singletonList(tags.getFirst()).toArray(new String[0])
                )
            ).thenReturn(0);
            PostPageResponseDto response = postService.findPostsBySearchString(tags.getFirst(), pageRequest);
            assertNotNull(response);
            assertNotNull(response);
            assertTrue(response.getPosts().isEmpty());
            assertEquals(FALSE, response.getHasPrev());
            assertEquals(FALSE, response.getHasNext());
            assertEquals(0, response.getLastPage());
            verify(postRepository, times(1)).findTotalBySearch(SELECT_COUNT,
                new String[]{},
                singletonList(tags.getFirst()).toArray(new String[0])
            );
            verify(postRepository, times(0)).findBySearch(eq(SELECT_DATA), any(), any(), any());
            verify(tagRepository, times(0)).findTagsByPostIdIn(any());
            verify(commentRepository, times(0)).countCommentsByPostIds(any());
        }

        @Test
        public void shouldFindPostsBySearchWithValidData() {
            when(postRepository.findTotalBySearch(
                    SELECT_COUNT,
                    new String[]{},
                    singletonList(tags.getFirst()).toArray(new String[0])
                )
            ).thenReturn(5);
            when(postRepository.findBySearch(
                SELECT_DATA,
                new String[]{},
                singletonList(tags.getFirst()).toArray(new String[0]),
                pageRequest
            )).thenReturn(singletonList(responseDto));
            when(tagRepository.findTagsByPostIdIn(anyCollection())).thenReturn(
                Map.of(postId, singletonList(new PostTagDto(postId, postTitle)))
            );
            when(commentRepository.countCommentsByPostIds(anyCollection())).thenReturn(Map.of(postId, 5L));
            PostPageResponseDto response = postService.findPostsBySearchString(tags.getFirst(), pageRequest);
            assertNotNull(response);
            assertNotNull(response);
            assertFalse(response.getPosts().isEmpty());
            assertEquals(FALSE, response.getHasPrev());
            assertEquals(TRUE, response.getHasNext());
            assertEquals(TRUE, response.getHasNext());
            assertEquals(5, response.getLastPage());
            PostResponseDto postResponseDto = response.getPosts().iterator().next();
            assertEquals(5, postResponseDto.getCommentsCount());
            assertEquals(1, postResponseDto.getLikesCount());
            assertEquals(postTitle, postResponseDto.getTitle());
            verify(postRepository, times(1)).findTotalBySearch(SELECT_COUNT,
                new String[]{},
                singletonList(tags.getFirst()).toArray(new String[0])
            );
            verify(postRepository, times(1)).findBySearch(
                SELECT_DATA,
                new String[]{},
                singletonList(tags.getFirst()).toArray(new String[0]),
                pageRequest
            );
            verify(tagRepository, times(1)).findTagsByPostIdIn(anyCollection());
            verify(commentRepository, times(1)).countCommentsByPostIds(anyCollection());
        }
    }
}

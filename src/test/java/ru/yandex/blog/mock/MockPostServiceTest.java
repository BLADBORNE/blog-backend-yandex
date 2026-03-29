package ru.yandex.blog.mock;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.blog.dto.post.PostCreateRequestDto;
import ru.yandex.blog.dto.post.PostPageResponseDto;
import ru.yandex.blog.dto.post.PostResponseDto;
import ru.yandex.blog.dto.post.PostUpdateRequestDto;
import ru.yandex.blog.dto.tag.PostTagDto;
import ru.yandex.blog.dto.tag.PostTagInsertDto;
import ru.yandex.blog.dto.tag.TagDto;
import ru.yandex.blog.page.PageableRequest;
import ru.yandex.blog.repository.PostRepository;
import ru.yandex.blog.service.CommentService;
import ru.yandex.blog.service.TagService;
import ru.yandex.blog.service.impl.PostServiceImpl;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Long.MAX_VALUE;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static ru.yandex.blog.model.SqlOperation.SELECT_COUNT;
import static ru.yandex.blog.model.SqlOperation.SELECT_DATA;

@ExtendWith(MockitoExtension.class)
class MockPostServiceTest {

    @InjectMocks
    private PostServiceImpl postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private TagService tagService;

    @Mock
    private CommentService commenService;

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

    @Test
    void shouldSavePost() {
        PostCreateRequestDto requestDto = PostCreateRequestDto.builder().title(postTitle).text(postText).tags(tags).build();
        when(postRepository.save(requestDto)).thenReturn(responseDto);
        when(tagService.save(tags)).thenReturn(singletonList(tagDto));
        doNothing().when(tagService).saveTagsToPost(insertDto);
        PostResponseDto response = postService.save(requestDto);
        assertNotNull(response);
        assertEquals(1L, response.getCommentsCount());
        assertEquals(tags, response.getTags());
        assertEquals(postTitle, response.getTitle());
        ArgumentCaptor<PostCreateRequestDto> createPostCaptor = ArgumentCaptor.forClass(PostCreateRequestDto.class);
        verify(postRepository, times(1)).save(createPostCaptor.capture());
        assertEquals(postTitle, createPostCaptor.getValue().getTitle());
        assertEquals(postText, createPostCaptor.getValue().getText());
        verify(tagService, times(1)).save(tags);
        ArgumentCaptor<PostTagInsertDto> insertCaptorDto = ArgumentCaptor.forClass(PostTagInsertDto.class);
        verify(tagService, times(1)).saveTagsToPost(insertCaptorDto.capture());
        assertEquals(postId, insertCaptorDto.getValue().postId());
        assertEquals(tags.getLast(), tags.getLast());
    }

    @Nested
    class WhenUpdatePost {
        @Test
        void shouldUpdateWhenPostExists() {
            when(postRepository.existsById(postId)).thenReturn(TRUE);
            when(postRepository.update(updateRequestDto)).thenReturn(responseDto);
            doNothing().when(tagService).deleteTagsByPostId(postId);
            when(tagService.save(tags)).thenReturn(singletonList(tagDto));
            doNothing().when(tagService).saveTagsToPost(insertDto);
            when(commenService.countCommentsByPostId(postId)).thenReturn(1L);
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
            verify(tagService, times(1)).deleteTagsByPostId(postId);
            verify(tagService, times(1)).save(tags);
            ArgumentCaptor<PostTagInsertDto> insertCaptorDto = ArgumentCaptor.forClass(PostTagInsertDto.class);
            verify(tagService, times(1)).saveTagsToPost(insertCaptorDto.capture());
            assertEquals(postId, insertCaptorDto.getValue().postId());
            assertEquals(tags.getLast(), tags.getLast());
            verify(commenService, times(1)).countCommentsByPostId(postId);
        }

        @Test
        void shouldUpdateWhenPostNotExists() {
            when(postRepository.existsById(postId)).thenReturn(FALSE);
            assertThrows(NoSuchElementException.class, () -> postService.update(updateRequestDto));
            verify(postRepository, times(1)).existsById(postId);
            ArgumentCaptor<PostUpdateRequestDto> updatePostCaptor = ArgumentCaptor.forClass(PostUpdateRequestDto.class);
            verify(postRepository, times(0)).update(updatePostCaptor.capture());
            verify(tagService, times(0)).deleteTagsByPostId(postId);
            verify(tagService, times(0)).save(tags);
            ArgumentCaptor<PostTagInsertDto> insertCaptorDto = ArgumentCaptor.forClass(PostTagInsertDto.class);
            verify(tagService, times(0)).saveTagsToPost(insertCaptorDto.capture());
            verify(commenService, times(0)).countCommentsByPostId(postId);
        }
    }

    @Nested
    class WhenIncreaseLike {
        @Test
        void shouldIncreaseLikeWhenPostExists() {
            when(postRepository.existsById(postId)).thenReturn(TRUE);
            when(postRepository.increaseLike(postId)).thenReturn(1);
            Integer response = postService.increaseLike(postId);
            assertEquals(1, response);
            verify(postRepository, times(1)).existsById(postId);
            verify(postRepository, times(1)).increaseLike(postId);
        }

        @Test
        void shouldIncreaseLikeWhenPostNExists() {
            when(postRepository.existsById(postId)).thenReturn(FALSE);
            assertThrows(NoSuchElementException.class, () -> postService.increaseLike(postId));
            verify(postRepository, times(1)).existsById(postId);
            verify(postRepository, times(0)).increaseLike(postId);
        }
    }

    @Nested
    class WhenFindPostById {
        @Test
        void shouldFindPostByIdWhenExists() {
            when(postRepository.findById(postId)).thenReturn(Optional.of(responseDto));
            when(tagService.findByPostId(postId)).thenReturn(singletonList(tagDto));
            when(commenService.countCommentsByPostId(postId)).thenReturn(2L);
            PostResponseDto response = postService.findById(postId);
            assertNotNull(response);
            assertEquals(postTitle, response.getTitle());
            assertEquals(postText, response.getText());
            assertEquals(tags.getLast(), tags.getLast());
            assertEquals(2L, response.getCommentsCount());
            verify(postRepository, times(1)).findById(postId);
            verify(tagService, times(1)).findByPostId(postId);
            verify(commenService, times(1)).countCommentsByPostId(postId);
        }

        @Test
        void shouldFindPostByIdWhenNotExists() {
            when(postRepository.findById(postId)).thenReturn(Optional.empty());
            assertThrows(NoSuchElementException.class, () -> postService.findById(postId));
            verify(postRepository, times(1)).findById(postId);
            verify(tagService, times(0)).findByPostId(postId);
            verify(commenService, times(0)).countCommentsByPostId(postId);
        }
    }

    @Nested
    class WhenExistsById {
        @Test
        void shouldExistsById() {
            when(postRepository.existsById(postId)).thenReturn(TRUE);
            Boolean result = postService.existsById(postId);
            assertNotNull(result);
            assertEquals(TRUE, result);
            verify(postRepository, times(1)).existsById(postId);
        }

        @Test
        void shouldNotExistsById() {
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
        void shouldDeleteByIdWhenPostExists() {
            when(postRepository.existsById(postId)).thenReturn(TRUE);
            doNothing().when(postRepository).deleteById(postId);
            postService.deleteById(postId);
            verify(postRepository, times(1)).existsById(postId);
            verify(postRepository, times(1)).deleteById(postId);
        }

        @Test
        void shouldDeleteByIdWhenPostNotExists() {
            when(postRepository.existsById(postId)).thenReturn(FALSE);
            assertThrows(NoSuchElementException.class, () -> postService.deleteById(postId));
            verify(postRepository, times(1)).existsById(postId);
            verify(postRepository, times(0)).deleteById(postId);
        }
    }

    @Nested
    class WhenShouldFindPostsBySearchString {
        @Test
        void shouldFindPostsBySearchStringWhenTagsAndWordsAreEmpty() {
            PostPageResponseDto response = postService.findPostsBySearchString(notValidString, pageRequest);
            assertNotNull(response);
            assertNotNull(response);
            assertTrue(response.getPosts().isEmpty());
            assertEquals(FALSE, response.getHasPrev());
            assertEquals(FALSE, response.getHasNext());
            assertEquals(0, response.getLastPage());
            verify(postRepository, times(0)).findTotalBySearch(eq(SELECT_COUNT), any(), any());
            verify(postRepository, times(0)).findBySearch(eq(SELECT_DATA), any(), any(), any());
            verify(tagService, times(0)).findTagsByPostIdIn(any());
            verify(commenService, times(0)).countCommentsByPostIds(any());
        }

        @Test
        void shouldFindPostsBySearchStringWhenPageGreaterThanResult() {
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
            verify(tagService, times(0)).findTagsByPostIdIn(any());
            verify(commenService, times(0)).countCommentsByPostIds(any());
        }

        @Test
        void shouldFindPostsBySearchWithValidData() {
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
            when(tagService.findTagsByPostIdIn(anyCollection())).thenReturn(
                Map.of(postId, singletonList(new PostTagDto(postId, postTitle)))
            );
            when(commenService.countCommentsByPostIds(anyCollection())).thenReturn(Map.of(postId, 5L));
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
            verify(tagService, times(1)).findTagsByPostIdIn(anyCollection());
            verify(commenService, times(1)).countCommentsByPostIds(anyCollection());
        }
    }
}

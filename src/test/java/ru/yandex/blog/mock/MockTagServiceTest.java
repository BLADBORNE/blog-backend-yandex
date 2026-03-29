package ru.yandex.blog.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.blog.dto.tag.PostTagDto;
import ru.yandex.blog.dto.tag.PostTagInsertDto;
import ru.yandex.blog.dto.tag.TagDto;
import ru.yandex.blog.repository.TagRepository;
import ru.yandex.blog.service.impl.TagServiceImpl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MockTagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagServiceImpl tagService;

    private final List<String> tags = List.of("programming", "java");

    private final TagDto tagDto = new TagDto(1L, tags.getFirst());

    private final Long postId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);

    private final ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);

    @Test
    void shouldFindByPostId() {
        when(tagRepository.findByPostId(postId)).thenReturn(List.of(tagDto));
        List<TagDto> tags = tagService.findByPostId(postId);
        verify(tagRepository, times(1)).findByPostId(postId);
        assertFalse(tags.isEmpty());
        assertEquals(tagDto.title(), tags.getFirst().title());
    }

    @Test
    void shouldSaveTags() {
        when(tagRepository.save(tags)).thenReturn(List.of(tagDto));
        List<TagDto> savedTags = tagService.save(tags);
        verify(tagRepository, times(1)).save(tags);
        assertFalse(savedTags.isEmpty());
        assertEquals(tagDto.title(), savedTags.getFirst().title());
    }

    @Test
    void shouldSaveTagsToPost() {
        PostTagInsertDto insertDto = new PostTagInsertDto(1L, List.of(1L));
        doNothing().when(tagRepository).saveTagsToPost(insertDto);
        tagService.saveTagsToPost(insertDto);
        verify(tagRepository, times(1)).saveTagsToPost(insertDto);
    }

    @Test
    void shouldFindTagsByPostIdIn() {
        PostTagDto dto = new PostTagDto(postId, tags.getFirst());
        when(tagRepository.findTagsByPostIdIn(singletonList(postId))).thenReturn(Map.of(1L, singletonList(dto)));
        Map<Long, List<PostTagDto>> result = tagService.findTagsByPostIdIn(singletonList(postId));
        assertFalse(result.isEmpty());
        assertEquals(postId, result.get(1L).getFirst().postId());
        verify(tagRepository, times(1)).findTagsByPostIdIn(singletonList(postId));
    }

    @Test
    void shouldDeleteTagsByPostId() {
        doNothing().when(tagRepository).deleteTagsByPostId(postId);
        tagService.deleteTagsByPostId(postId);
        verify(tagRepository, times(1)).deleteTagsByPostId(longCaptor.capture());
        assertEquals(postId, longCaptor.getValue());
    }
}

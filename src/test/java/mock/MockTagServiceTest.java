package mock;

import configuration.mock.MockTagServiceConfiguration;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.blog.backend.dto.tag.PostTagDto;
import ru.blog.backend.dto.tag.PostTagInsertDto;
import ru.blog.backend.dto.tag.TagDto;
import ru.blog.backend.repository.TagRepository;
import ru.blog.backend.service.TagService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doNothing;

@SpringJUnitConfig(MockTagServiceConfiguration.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MockTagServiceTest {

    private final TagRepository tagRepository;

    private final TagService tagService;

    private final List<String> tags = List.of("programming", "java");

    private final TagDto tagDto = new TagDto(1L, tags.getFirst());

    private final Long postId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);

    private final ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);

    @BeforeEach
    void resetMocks() {
        reset(tagRepository);
    }

    @Test
    public void shouldFindByPostId() {
        when(tagRepository.findByPostId(postId)).thenReturn(List.of(tagDto));
        List<TagDto> tags = tagService.findByPostId(postId);
        verify(tagRepository, times(1)).findByPostId(postId);
        assertFalse(tags.isEmpty());
        assertEquals(tagDto.title(), tags.getFirst().title());
    }

    @Test
    public void shouldSaveTags() {
        when(tagRepository.save(tags)).thenReturn(List.of(tagDto));
        List<TagDto> savedTags = tagService.save(tags);
        verify(tagRepository, times(1)).save(tags);
        assertFalse(savedTags.isEmpty());
        assertEquals(tagDto.title(), savedTags.getFirst().title());
    }

    @Test
    public void shouldSaveTagsToPost() {
        PostTagInsertDto insertDto = new PostTagInsertDto(1L, List.of(1L));
        doNothing().when(tagRepository).saveTagsToPost(insertDto);
        tagService.saveTagsToPost(insertDto);
        verify(tagRepository, times(1)).saveTagsToPost(insertDto);
    }

    @Test
    public void shouldFindTagsByPostIdIn() {
        PostTagDto dto = new PostTagDto(postId, tags.getFirst());
        when(tagRepository.findTagsByPostIdIn(singletonList(postId))).thenReturn(Map.of(1L, singletonList(dto)));
        Map<Long, List<PostTagDto>> result = tagService.findTagsByPostIdIn(singletonList(postId));
        assertFalse(result.isEmpty());
        assertEquals(postId, result.get(1L).getFirst().postId());
        verify(tagRepository, times(1)).findTagsByPostIdIn(singletonList(postId));
    }

    @Test
    public void shouldDeleteTagsByPostId() {
        doNothing().when(tagRepository).deleteTagsByPostId(postId);
        tagService.deleteTagsByPostId(postId);
        verify(tagRepository, times(1)).deleteTagsByPostId(longCaptor.capture());
        assertEquals(postId,longCaptor.getValue());
    }
}

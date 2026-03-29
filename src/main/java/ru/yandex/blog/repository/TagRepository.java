package ru.yandex.blog.repository;

import ru.yandex.blog.dto.tag.PostTagDto;
import ru.yandex.blog.dto.tag.PostTagInsertDto;
import ru.yandex.blog.dto.tag.TagDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface TagRepository {

    List<TagDto> findByPostId(Long postId);

    List<TagDto> save(List<String> tags);

    void saveTagsToPost(PostTagInsertDto postTagInsertDto);

    Map<Long, List<PostTagDto>> findTagsByPostIdIn(Collection<Long> postIds);

    void deleteTagsByPostId(Long postId);
}

package ru.blog.backend.repository;

import ru.blog.backend.dto.tag.PostTagDto;
import ru.blog.backend.dto.tag.PostTagInsertDto;
import ru.blog.backend.dto.tag.TagDto;

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

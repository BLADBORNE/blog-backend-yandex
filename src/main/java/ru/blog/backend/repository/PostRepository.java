package ru.blog.backend.repository;

import ru.blog.backend.dto.post.PostResponseDto;
import ru.blog.backend.model.SqlOperation;
import ru.blog.backend.page.PageableRequest;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends PostCommon {

    Integer findTotalBySearch(SqlOperation sqlOperation, String[] tags, String[] words);

    List<PostResponseDto> findBySearch(
        SqlOperation sqlOperation,
        String[] tags,
        String[] words,
        PageableRequest pageableRequest
    );

    Optional<PostResponseDto> findById(Long id);
}

package ru.yandex.blog.repository;

import ru.yandex.blog.dto.post.PostResponseDto;
import ru.yandex.blog.model.SqlOperation;
import ru.yandex.blog.page.PageableRequest;

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

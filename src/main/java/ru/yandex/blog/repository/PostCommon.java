package ru.yandex.blog.repository;

import ru.yandex.blog.dto.post.PostCreateRequestDto;
import ru.yandex.blog.dto.post.PostResponseDto;
import ru.yandex.blog.dto.post.PostUpdateRequestDto;

public interface PostCommon extends CommonExists, CommonAction {

    PostResponseDto save(PostCreateRequestDto request);

    PostResponseDto update(PostUpdateRequestDto request);

    Integer increaseLike(Long postId);
}

package ru.blog.backend.repository;

import ru.blog.backend.dto.post.PostCreateRequestDto;
import ru.blog.backend.dto.post.PostResponseDto;
import ru.blog.backend.dto.post.PostUpdateRequestDto;

public interface PostCommon extends CommonExists, CommonAction {

    PostResponseDto save(PostCreateRequestDto request);

    PostResponseDto update(PostUpdateRequestDto request);

    Integer increaseLike(Long postId);
}

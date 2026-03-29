package ru.yandex.blog.repository;

import ru.yandex.blog.dto.comment.CommentDto;

import java.util.Optional;

public interface CommentRepository extends CommentCommon, CommonExists {

    Optional<CommentDto> findCommentById(Long commentId);
}

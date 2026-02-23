package ru.blog.backend.repository;

import ru.blog.backend.dto.comment.CommentDto;

import java.util.Optional;

public interface CommentRepository extends CommentCommon, CommonExists {

    Optional<CommentDto> findCommentById(Long commentId);
}

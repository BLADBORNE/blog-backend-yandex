package ru.blog.backend.service;

import ru.blog.backend.dto.comment.CommentDto;
import ru.blog.backend.repository.CommentCommon;

public interface CommentService extends CommentCommon {

    CommentDto findCommentByPostIdAndCommentId(Long postId, Long commentId);
}

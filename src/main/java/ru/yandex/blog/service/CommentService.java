package ru.yandex.blog.service;

import ru.yandex.blog.dto.comment.CommentDto;
import ru.yandex.blog.repository.CommentCommon;

public interface CommentService extends CommentCommon {

    CommentDto findCommentByPostIdAndCommentId(Long postId, Long commentId);
}

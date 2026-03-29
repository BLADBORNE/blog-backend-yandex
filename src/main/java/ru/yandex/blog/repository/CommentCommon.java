package ru.yandex.blog.repository;

import ru.yandex.blog.dto.comment.CommentCreateRequestDto;
import ru.yandex.blog.dto.comment.CommentDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface CommentCommon extends CommonAction {

    CommentDto addCommentToPost(CommentCreateRequestDto requestDto);

    List<CommentDto> findCommentsByPostId(Long postId);

    CommentDto updatePostComment(CommentDto requestDto);

    Long countCommentsByPostId(Long postId);

    Map<Long, Long> countCommentsByPostIds(Collection<Long> postIds);
}

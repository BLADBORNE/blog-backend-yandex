package ru.blog.backend.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.blog.backend.dto.comment.CommentDto;
import ru.blog.backend.dto.comment.CommentCreateRequestDto;
import ru.blog.backend.repository.CommentRepository;
import ru.blog.backend.service.CommentService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository repository;

    private final PostExistenceChecker postExistenceChecker;

    public CommentServiceImpl(
        @Qualifier("commentRepositoryImpl") CommentRepository repository,
        PostExistenceChecker postExistenceChecker
    ) {
        this.repository = repository;
        this.postExistenceChecker = postExistenceChecker;
    }

    @Override
    @Transactional
    public CommentDto addCommentToPost(CommentCreateRequestDto requestDto) {
        postExistenceChecker.requirePost(requestDto.getPostId());

        return repository.addCommentToPost(requestDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> findCommentsByPostId(Long postId) {
        postExistenceChecker.requirePost(postId);

        return repository.findCommentsByPostId(postId);
    }

    @Override
    @Transactional
    public CommentDto updatePostComment(CommentDto requestDto) {
        checkCommentExists(requestDto.getId());

        return repository.updatePostComment(requestDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countCommentsByPostId(Long postId) {
        return repository.countCommentsByPostId(postId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> countCommentsByPostIds(Collection<Long> postIds) {
        return repository.countCommentsByPostIds(postIds);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto findCommentByPostIdAndCommentId(Long postId, Long commentId) {
        postExistenceChecker.requirePost(postId);

        return repository.findCommentById(commentId)
            .orElseThrow(() -> new NoSuchElementException("Отсутствует комментарий с id = %s".formatted(commentId)));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        checkCommentExists(id);
        repository.deleteById(id);
    }

    private void checkCommentExists(Long commentId) {
        if (!repository.existsById(commentId)) {
            throw new NoSuchElementException("Отсутствует комментарий с id = %s".formatted(commentId));
        }
    }
}

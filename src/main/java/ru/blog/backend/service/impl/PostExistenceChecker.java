package ru.blog.backend.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.blog.backend.repository.PostRepository;

import java.util.NoSuchElementException;

@Component
public class PostExistenceChecker {

    private final PostRepository repository;

    public PostExistenceChecker(@Qualifier("postRepositoryImpl") PostRepository repository) {
        this.repository = repository;
    }

    public void requirePost(Long postId) {
        if (!repository.existsById(postId)) {
            throw new NoSuchElementException("Отсутствует пост с id = %s".formatted(postId));
        }
    }
}

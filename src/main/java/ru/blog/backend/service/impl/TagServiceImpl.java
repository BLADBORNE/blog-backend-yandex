package ru.blog.backend.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.blog.backend.dto.tag.PostTagDto;
import ru.blog.backend.dto.tag.PostTagInsertDto;
import ru.blog.backend.dto.tag.TagDto;
import ru.blog.backend.repository.TagRepository;
import ru.blog.backend.service.TagService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class TagServiceImpl implements TagService {

    private final TagRepository repository;

    public TagServiceImpl(@Qualifier("tagRepositoryImpl") TagRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagDto> findByPostId(Long postId) {
        return repository.findByPostId(postId);
    }

    @Override
    @Transactional
    public List<TagDto> save(List<String> tags) {
        return repository.save(tags);
    }

    @Override
    @Transactional
    public void saveTagsToPost(PostTagInsertDto postTagInsertDto) {
        repository.saveTagsToPost(postTagInsertDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<PostTagDto>> findTagsByPostIdIn(Collection<Long> postIds) {
        return repository.findTagsByPostIdIn(postIds);
    }

    @Override
    @Transactional
    public void deleteTagsByPostId(Long postId) {
        repository.deleteTagsByPostId(postId);
    }
}

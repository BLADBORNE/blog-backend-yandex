package ru.blog.backend.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.blog.backend.dto.post.PostCreateRequestDto;
import ru.blog.backend.dto.post.PostPageResponseDto;
import ru.blog.backend.dto.post.PostResponseDto;
import ru.blog.backend.dto.post.PostUpdateRequestDto;
import ru.blog.backend.dto.tag.PostTagDto;
import ru.blog.backend.dto.tag.PostTagInsertDto;
import ru.blog.backend.dto.tag.TagDto;
import ru.blog.backend.page.PageableRequest;
import ru.blog.backend.repository.PostRepository;
import ru.blog.backend.service.CommentService;
import ru.blog.backend.service.PostService;
import ru.blog.backend.service.TagService;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toMap;
import static ru.blog.backend.model.SqlOperation.SELECT_COUNT;
import static ru.blog.backend.model.SqlOperation.SELECT_DATA;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository repository;

    private final TagService tagService;

    private final CommentService commentService;

    private static final Integer FIRST_PAGE = 1;

    private static final Integer START_TAG_INDEX = 1;

    private static final Integer MAX_VISIBLE_STRING_LENGTH = 128;

    public PostServiceImpl(
        @Qualifier("postRepositoryImpl") PostRepository repository,
        TagService tagService,
        CommentService commentService
    ) {
        this.repository = repository;
        this.tagService = tagService;
        this.commentService = commentService;
    }

    @Override
    @Transactional
    public PostResponseDto save(PostCreateRequestDto request) {
        PostResponseDto response = repository.save(request);
        saveTagsToPost(response.getId(), request.getTags());

        return response.toBuilder().tags(request.getTags()).build();
    }

    @Override
    @Transactional
    public PostResponseDto update(PostUpdateRequestDto request) {
        checkPostExists(request.getId());
        PostResponseDto result = repository.update(request);
        tagService.deleteTagsByPostId(request.getId());
        saveTagsToPost(request.getId(), request.getTags());

        return result.toBuilder()
            .tags(request.getTags())
            .commentsCount(commentService.countCommentsByPostId(result.getId()))
            .build();
    }

    @Override
    @Transactional
    public Integer increaseLike(Long postId) {
        checkPostExists(postId);

        return repository.increaseLike(postId);
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponseDto findById(Long id) {
        return repository.findById(id)
            .map(r -> r.toBuilder()
                .tags(tagService.findByPostId(id).stream().map(TagDto::title).toList())
                .commentsCount(commentService.countCommentsByPostId(id))
                .build()
            )
            .orElseThrow(() -> new NoSuchElementException("Отсутствует пост с id = %s".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        checkPostExists(id);
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PostPageResponseDto findPostsBySearchString(String search, PageableRequest pageableRequest) {
        List<String> tags = new ArrayList<>();
        List<String> words = new ArrayList<>();
        Arrays.stream(search.split(" "))
            .filter(Predicate.not(String::isBlank))
            .forEach(p -> {
                if (p.startsWith("#")) {
                    tags.add(p.substring(START_TAG_INDEX));
                } else {
                    words.add(p);
                }
            });

        if (tags.isEmpty() && words.isEmpty()) {
            return PostPageResponseDto.empty();
        }

        Integer total = repository.findTotalBySearch(
            SELECT_COUNT,
            tags.toArray(new String[0]),
            words.toArray(new String[0])
        );
        int totalPages = (int) Math.ceil((double) total / pageableRequest.pageSize());

        if (total == 0 || pageableRequest.pageNumber() > totalPages) {
            return PostPageResponseDto.empty();
        }

        Map<Long, PostResponseDto> values = repository.findBySearch(
            SELECT_DATA,
            tags.toArray(new String[0]),
            words.toArray(new String[0]),
            pageableRequest
        ).stream().collect(toMap(PostResponseDto::getId, Function.identity()));
        Map<Long, List<PostTagDto>> postsTags = tagService.findTagsByPostIdIn(values.keySet());
        Map<Long, Long> comments = commentService.countCommentsByPostIds(values.keySet());
        values.forEach((Long postId, PostResponseDto d) -> {
            d.setTags(postsTags.getOrDefault(postId, Collections.emptyList()).stream().map(PostTagDto::title).toList());
            d.setCommentsCount(comments.getOrDefault(postId, 0L));
            d.setText(trimStringLength(d.getText()));
        });

        return PostPageResponseDto.initResponse(
            values.values(),
            !FIRST_PAGE.equals(pageableRequest.pageNumber()),
            !pageableRequest.pageNumber().equals(totalPages),
            totalPages
        );
    }

    private void checkPostExists(Long postId) {
        if (!repository.existsById(postId)) {
            throw new NoSuchElementException("Отсутствует пост с id = %s".formatted(postId));
        }
    }

    private void saveTagsToPost(Long postId, List<String> tags) {
        tagService.saveTagsToPost(new PostTagInsertDto(postId, tagService.save(tags).stream().map(TagDto::id).toList()));
    }

    private String trimStringLength(String value) {
        if (value.length() > MAX_VISIBLE_STRING_LENGTH) {
            return value.substring(0, MAX_VISIBLE_STRING_LENGTH) + "...";
        }
        return value;
    }
}

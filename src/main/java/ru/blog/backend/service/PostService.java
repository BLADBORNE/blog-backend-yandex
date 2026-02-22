package ru.blog.backend.service;

import ru.blog.backend.dto.post.PostPageResponseDto;
import ru.blog.backend.dto.post.PostResponseDto;
import ru.blog.backend.page.PageableRequest;
import ru.blog.backend.repository.PostCommon;

public interface PostService extends PostCommon {

    PostPageResponseDto findPostsBySearchString(String search, PageableRequest pageableRequest);

    PostResponseDto findById(Long id);
}

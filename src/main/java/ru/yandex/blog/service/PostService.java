package ru.yandex.blog.service;

import ru.yandex.blog.dto.post.PostPageResponseDto;
import ru.yandex.blog.dto.post.PostResponseDto;
import ru.yandex.blog.page.PageableRequest;
import ru.yandex.blog.repository.PostCommon;

public interface PostService extends PostCommon {

    PostPageResponseDto findPostsBySearchString(String search, PageableRequest pageableRequest);

    PostResponseDto findById(Long id);
}

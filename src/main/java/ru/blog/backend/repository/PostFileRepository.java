package ru.blog.backend.repository;

import ru.blog.backend.dto.file.PostFileDto;

import java.util.Optional;

public interface PostFileRepository extends CommonExists {

    Optional<PostFileDto> findFileByPostId(Long postId);

    void savePostFileInfo(PostFileDto postFileDto);

    void updatePostFileInfo(PostFileDto postFileDto);
}

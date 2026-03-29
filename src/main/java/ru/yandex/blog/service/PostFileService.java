package ru.yandex.blog.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface PostFileService {

    void updatePostFile(MultipartFile multipartFile, Long postId);

    Resource findFileByPostId(Long postId);
}

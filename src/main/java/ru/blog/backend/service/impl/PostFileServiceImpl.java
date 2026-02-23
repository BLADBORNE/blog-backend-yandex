package ru.blog.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.blog.backend.dto.file.PostFileDto;
import ru.blog.backend.exception.FileTypeNotSupportedException;
import ru.blog.backend.model.FileType;
import ru.blog.backend.repository.PostFileRepository;
import ru.blog.backend.service.PostFileService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

@Service
public class PostFileServiceImpl implements PostFileService {

    private final PostExistenceChecker postExistenceChecker;

    private final PostFileRepository postFileRepository;

    private final String fileDir;

    @Autowired
    public PostFileServiceImpl(
        PostExistenceChecker postExistenceChecker,
        PostFileRepository postFileRepository,
        @Value("${file.dir}") String fileDir
    ) {
        this.postExistenceChecker = postExistenceChecker;
        this.postFileRepository = postFileRepository;
        this.fileDir = fileDir;
    }

    @Override
    @Transactional
    public void updatePostFile(MultipartFile multipartFile, Long postId) {
        postExistenceChecker.requirePost(postId);
        FileType fileType = FileType.parseType(multipartFile.getContentType());

        if (fileType == null) {
            throw new FileTypeNotSupportedException(
                "Тип контента %s не поддерживается на сервере".formatted(multipartFile.getContentType())
            );
        }

        String fileName;
        try {
            Path uploadDir = Paths.get(fileDir);

            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            fileName = setUniqueFileName(fileType, postId);
            Path filePath = uploadDir.resolve(fileName);
            multipartFile.transferTo(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        if (!postFileRepository.existsById(postId)) {
            postFileRepository.savePostFileInfo(new PostFileDto(fileName, postId));
        } else {
            postFileRepository.updatePostFileInfo(new PostFileDto(fileName, postId));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Resource findFileByPostId(Long postId) {
        postExistenceChecker.requirePost(postId);
        PostFileDto fileDto = postFileRepository.findFileByPostId(postId)
            .orElseThrow(() -> new NoSuchElementException("Отсутвует фотограия у поста с id = %s".formatted(postId)));
        Path filePath = Paths.get(fileDir).resolve(fileDto.fileName()).normalize();

        return new FileSystemResource(filePath);
    }

    private String setUniqueFileName(FileType fileType, Long postId) {

        return "%s.%s".formatted(postId, fileType.toString().toLowerCase());
    }
}

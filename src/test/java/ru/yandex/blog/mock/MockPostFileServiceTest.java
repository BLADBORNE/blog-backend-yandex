package ru.yandex.blog.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.blog.dto.file.PostFileDto;
import ru.yandex.blog.exception.FileTypeNotSupportedException;
import ru.yandex.blog.repository.PostFileRepository;
import ru.yandex.blog.service.impl.PostExistenceChecker;
import ru.yandex.blog.service.impl.PostFileServiceImpl;

import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@ExtendWith(SpringExtension.class)
class MockPostFileServiceTest {

    @Mock
    private PostFileRepository postFileRepository;

    @InjectMocks
    private PostFileServiceImpl postFileService;

    @Mock
    private PostExistenceChecker postExistenceChecker;

    private final String fileName = "avatar.png";

    private final MockMultipartFile file = new MockMultipartFile(
        "file",
        null,
        IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4}
    );

    private final Long postId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);

    private final ArgumentCaptor<PostFileDto> captorPostField = ArgumentCaptor.forClass(PostFileDto.class);

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        postFileService = new PostFileServiceImpl(
            postExistenceChecker,
            postFileRepository,
            tempDir.toString()
        );
    }

    @Nested
    class WhenUpdatePostFile {
        @Test
        void shouldSavePostFileWhenPostExistsAndFileTypeIsCorrect() {
            doNothing().when(postExistenceChecker).requirePost(postId);
            when(postFileRepository.existsById(postId)).thenReturn(FALSE);
            postFileService.updatePostFile(file, postId);
            verify(postExistenceChecker, times(1)).requirePost(postId);
            verify(postFileRepository, times(1)).existsById(postId);
            verify(postFileRepository, times(1)).savePostFileInfo(captorPostField.capture());
            verify(postFileRepository, times(0)).updatePostFileInfo(captorPostField.capture());
            assertEquals(postId, captorPostField.getValue().postId());
            assertTrue(captorPostField.getValue().fileName().contains(postId.toString()));
        }

        @Test
        void shouldUpdatePostFileWhenPostExistsAndFileTypeIsCorrect() {
            doNothing().when(postExistenceChecker).requirePost(postId);
            when(postFileRepository.existsById(postId)).thenReturn(TRUE);
            postFileService.updatePostFile(file, postId);
            verify(postExistenceChecker, times(1)).requirePost(postId);
            verify(postFileRepository, times(1)).existsById(postId);
            verify(postFileRepository, times(1)).updatePostFileInfo(captorPostField.capture());
            assertEquals(postId, captorPostField.getValue().postId());
            assertTrue(captorPostField.getValue().fileName().contains(postId.toString()));
        }

        @Test
        void shouldUpdatePostFileWhenPosNotExists() {
            doThrow(NoSuchElementException.class).when(postExistenceChecker).requirePost(postId);
            assertThrows(NoSuchElementException.class, () -> postFileService.updatePostFile(file, postId));
            verify(postExistenceChecker, times(1)).requirePost(postId);
            verify(postFileRepository, times(0)).existsById(postId);
            verify(postFileRepository, times(0)).updatePostFileInfo(captorPostField.capture());
            verify(postFileRepository, times(0)).savePostFileInfo(captorPostField.capture());
        }

        @Test
        void shouldUpdatePostFileWhenPosExistsAndTypeIsNotCorrect() {
            doNothing().when(postExistenceChecker).requirePost(postId);
            MultipartFile updatedFile = new MockMultipartFile(
                "file",
                null,
                APPLICATION_JSON_VALUE, new byte[]{1, 2, 3, 4}
            );
            RuntimeException e = assertThrows(FileTypeNotSupportedException.class, () -> postFileService.updatePostFile(
                updatedFile,
                postId
            ));
            verify(postExistenceChecker, times(1)).requirePost(postId);
            verify(postFileRepository, times(0)).existsById(postId);
            verify(postFileRepository, times(0)).updatePostFileInfo(captorPostField.capture());
            verify(postFileRepository, times(0)).savePostFileInfo(captorPostField.capture());
            assertEquals(
                "Тип контента %s не поддерживается на сервере".formatted(updatedFile.getContentType()),
                e.getMessage()
            );
        }

        @Nested
        class WhenFindFileByPostId {
            @Test
            void shouldFindFileByPostIdWhenPostExists() {
                doNothing().when(postExistenceChecker).requirePost(postId);
                when(postFileRepository.findFileByPostId(postId)).thenReturn(Optional.of(new PostFileDto(fileName, postId)));
                Resource result = postFileService.findFileByPostId(postId);
                assertNotNull(result);
                assertTrue(Objects.requireNonNull(result.getFilename()).contains(fileName));
                verify(postExistenceChecker, times(1)).requirePost(postId);
                verify(postFileRepository, times(1)).findFileByPostId(postId);
            }

            @Test
            void shouldFindFileByPostIdWhenPostNotExists() {
                doThrow(NoSuchElementException.class).when(postExistenceChecker).requirePost(postId);
                assertThrows(NoSuchElementException.class, () -> postFileService.findFileByPostId(postId));
                verify(postExistenceChecker, times(1)).requirePost(postId);
                verify(postFileRepository, times(0)).findFileByPostId(postId);
            }

            @Test
            void shouldFindFileByPostIdWhenPostExistsAndFileNotExists() {
                doNothing().when(postExistenceChecker).requirePost(postId);
                when(postFileRepository.findFileByPostId(111L)).thenReturn(Optional.empty());
                assertThrows(NoSuchElementException.class, () -> postFileService.findFileByPostId(postId));
                verify(postExistenceChecker, times(1)).requirePost(postId);
                verify(postFileRepository, times(1)).findFileByPostId(postId);
            }
        }
    }
}

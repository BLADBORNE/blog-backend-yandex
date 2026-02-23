package mock;

import configuration.mock.PostFileServiceConfiguration;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.multipart.MultipartFile;
import ru.blog.backend.dto.file.PostFileDto;
import ru.blog.backend.exception.FileTypeNotSupportedException;
import ru.blog.backend.repository.PostFileRepository;
import ru.blog.backend.repository.PostRepository;
import ru.blog.backend.service.PostFileService;

import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@SpringJUnitConfig(classes = PostFileServiceConfiguration.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MockPostFileServiceTest {

    private final PostFileRepository postFileRepository;

    private final PostFileService postFileService;

    private final PostRepository postRepository;

    private final String fileName = "avatar.png";

    private final MockMultipartFile file = new MockMultipartFile(
        "file",
        null,
        IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4}
    );

    private final Long postId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);

    private final ArgumentCaptor<PostFileDto> captorPostField = ArgumentCaptor.forClass(PostFileDto.class);

    @TempDir
    private static Path TEMP_DIR;

    @DynamicPropertySource
    private static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("file.dir", TEMP_DIR::toString);
    }

    @BeforeEach
    public void resetMocks() {
        reset(postFileRepository, postRepository);
    }

    @Nested
    class WhenUpdatePostFile {
        @Test
        public void shouldSavePostFileWhenPostExistsAndFileTypeIsCorrect() {
            when(postRepository.existsById(postId)).thenReturn(TRUE);
            when(postFileRepository.existsById(postId)).thenReturn(FALSE);
            postFileService.updatePostFile(file, postId);
            verify(postRepository, times(1)).existsById(postId);
            verify(postFileRepository, times(1)).existsById(postId);
            verify(postFileRepository, times(1)).savePostFileInfo(captorPostField.capture());
            verify(postFileRepository, times(0)).updatePostFileInfo(captorPostField.capture());
            assertEquals(postId, captorPostField.getValue().postId());
            assertTrue(captorPostField.getValue().fileName().contains(postId.toString()));
        }

        @Test
        public void shouldUpdatePostFileWhenPostExistsAndFileTypeIsCorrect() {
            when(postRepository.existsById(postId)).thenReturn(TRUE);
            when(postFileRepository.existsById(postId)).thenReturn(TRUE);
            postFileService.updatePostFile(file, postId);
            verify(postRepository, times(1)).existsById(postId);
            verify(postFileRepository, times(1)).existsById(postId);
            verify(postFileRepository, times(1)).updatePostFileInfo(captorPostField.capture());
            assertEquals(postId, captorPostField.getValue().postId());
            assertTrue(captorPostField.getValue().fileName().contains(postId.toString()));
        }

        @Test
        public void shouldUpdatePostFileWhenPosNotExists() {
            when(postRepository.existsById(postId)).thenReturn(FALSE);
            assertThrows(NoSuchElementException.class, () -> postFileService.updatePostFile(file, postId));
            verify(postRepository, times(1)).existsById(postId);
            verify(postFileRepository, times(0)).existsById(postId);
            verify(postFileRepository, times(0)).updatePostFileInfo(captorPostField.capture());
            verify(postFileRepository, times(0)).savePostFileInfo(captorPostField.capture());
        }

        @Test
        public void shouldUpdatePostFileWhenPosExistsAndTypeIsNotCorrect() {
            when(postRepository.existsById(postId)).thenReturn(TRUE);
            MultipartFile updatedFile = new MockMultipartFile(
                "file",
                null,
                APPLICATION_JSON_VALUE, new byte[]{1, 2, 3, 4}
            );
            RuntimeException e = assertThrows(FileTypeNotSupportedException.class, () -> postFileService.updatePostFile(
                updatedFile,
                postId
            ));
            verify(postRepository, times(1)).existsById(postId);
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
            public void shouldFindFileByPostIdWhenPostExists() {
                when(postRepository.existsById(postId)).thenReturn(TRUE);
                when(postFileRepository.findFileByPostId(postId)).thenReturn(Optional.of(new PostFileDto(fileName, postId)));
                Resource result = postFileService.findFileByPostId(postId);
                assertNotNull(result);
                assertTrue(Objects.requireNonNull(result.getFilename()).contains(fileName));
                verify(postRepository, times(1)).existsById(postId);
                verify(postFileRepository, times(1)).findFileByPostId(postId);
            }

            @Test
            public void shouldFindFileByPostIdWhenPostNotExists() {
                when(postRepository.existsById(postId)).thenReturn(FALSE);
                assertThrows(NoSuchElementException.class, () -> postFileService.findFileByPostId(postId));
                verify(postRepository, times(1)).existsById(postId);
                verify(postFileRepository, times(0)).findFileByPostId(postId);
            }

            @Test
            public void shouldFindFileByPostIdWhenPostExistsAndFileNotExists() {
                when(postRepository.existsById(postId)).thenReturn(TRUE);
                when(postFileRepository.findFileByPostId(111L)).thenReturn(Optional.empty());
                assertThrows(NoSuchElementException.class, () -> postFileService.findFileByPostId(postId));
                verify(postRepository, times(1)).existsById(postId);
                verify(postFileRepository, times(1)).findFileByPostId(postId);
            }
        }
    }
}

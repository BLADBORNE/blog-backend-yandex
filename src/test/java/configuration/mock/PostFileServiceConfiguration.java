package configuration.mock;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.blog.backend.repository.PostFileRepository;
import ru.blog.backend.service.PostFileService;
import ru.blog.backend.service.impl.PostExistenceChecker;
import ru.blog.backend.service.impl.PostFileServiceImpl;

@Configuration
@Import(MockPostExistenceCheckerConfiguration.class)
public class PostFileServiceConfiguration {

    @Bean
    public PostFileRepository initPostFileRepository() {
        return Mockito.mock(PostFileRepository.class);
    }

    @Bean
    public PostFileService initPostFileService(
        PostExistenceChecker postExistenceChecker,
        PostFileRepository postFileRepository,
        @Value("${file.dir}") String fileDir
    ) {
        return new PostFileServiceImpl(postExistenceChecker, postFileRepository, fileDir);
    }
}

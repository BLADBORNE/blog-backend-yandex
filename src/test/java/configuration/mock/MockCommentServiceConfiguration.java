package configuration.mock;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.blog.backend.repository.CommentRepository;
import ru.blog.backend.service.CommentService;
import ru.blog.backend.service.impl.CommentServiceImpl;
import ru.blog.backend.service.impl.PostExistenceChecker;

@Configuration
@Import(MockPostExistenceCheckerConfiguration.class)
public class MockCommentServiceConfiguration {

    @Bean
    public CommentRepository initCommentRepository() {
        return Mockito.mock(CommentRepository.class);
    }

    @Bean
    public CommentService initCommentService(
        CommentRepository commentRepository,
        PostExistenceChecker postExistenceChecker
    ) {
        return new CommentServiceImpl(commentRepository, postExistenceChecker);
    }
}

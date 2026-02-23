package configuration.integration.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.blog.backend.repository.CommentRepository;
import ru.blog.backend.repository.impl.CommentRepositoryImpl;
import ru.blog.backend.service.CommentService;
import ru.blog.backend.service.impl.CommentServiceImpl;
import ru.blog.backend.service.impl.PostExistenceChecker;

@Configuration
@Import(IntegrationPostExistenceCheckerConfiguration.class)
public class IntegrationCommentServiceConfiguration {

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public CommentRepository initCommentRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new CommentRepositoryImpl(namedParameterJdbcTemplate);
    }

    @Bean
    public CommentService initCommentService(
        CommentRepository commentRepository,
        PostExistenceChecker postExistenceChecker
    ) {
        return new CommentServiceImpl(commentRepository, postExistenceChecker);
    }
}

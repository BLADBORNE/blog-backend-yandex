package configuration.integration.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.blog.backend.repository.PostRepository;
import ru.blog.backend.service.impl.PostExistenceChecker;

@Configuration
@Import(IntegrationPostRepositoryConfiguration.class)
public class IntegrationPostExistenceCheckerConfiguration {

    @Bean
    public PostExistenceChecker initPostExistenceChecker(PostRepository postRepository) {
        return new PostExistenceChecker(postRepository);
    }
}

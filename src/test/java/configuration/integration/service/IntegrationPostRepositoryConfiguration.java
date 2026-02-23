package configuration.integration.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.blog.backend.repository.PostRepository;
import ru.blog.backend.repository.impl.PostRepositoryImpl;

@Configuration
public class IntegrationPostRepositoryConfiguration {

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public PostRepository initPostRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        return new PostRepositoryImpl(jdbcTemplate);
    }
}

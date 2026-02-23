package configuration.integration.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.blog.backend.repository.PostFileRepository;
import ru.blog.backend.repository.impl.PostFileRepositoryImpl;
import ru.blog.backend.service.PostFileService;
import ru.blog.backend.service.impl.PostExistenceChecker;
import ru.blog.backend.service.impl.PostFileServiceImpl;

@Configuration
@Import({IntegrationPostExistenceCheckerConfiguration.class})
public class IntegrationPostFileServiceConfiguration {

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public PostFileRepository initPostFileRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        return new PostFileRepositoryImpl(jdbcTemplate);
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

package configuration.integration.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.blog.backend.repository.TagRepository;
import ru.blog.backend.repository.impl.TagRepositoryImpl;
import ru.blog.backend.service.TagService;
import ru.blog.backend.service.impl.TagServiceImpl;

@Configuration
public class IntegrationTagServiceConfiguration {

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public TagRepository initTagRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new TagRepositoryImpl(namedParameterJdbcTemplate);
    }

    @Bean
    public TagService initTagService(TagRepository tagRepository) {
        return new TagServiceImpl(tagRepository);
    }
}

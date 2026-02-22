package configuration.mock;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ru.blog.backend.repository.TagRepository;
import ru.blog.backend.service.TagService;
import ru.blog.backend.service.impl.TagServiceImpl;

@Configuration
public class MockTagServiceConfiguration {

    @Bean
    @Primary
    public TagRepository initRepository() {
        return Mockito.mock(TagRepository.class);
    }

    @Bean
    public TagService initTagService(TagRepository tagRepository) {
        return new TagServiceImpl(tagRepository);
    }
}

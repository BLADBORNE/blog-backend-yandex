package configuration.mock;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.blog.backend.repository.PostRepository;
import ru.blog.backend.repository.impl.PostRepositoryImpl;

@Configuration
public class MockPostRepositoryConfiguration {

    @Bean
    public PostRepository initPostRepository() {
        return Mockito.mock(PostRepositoryImpl.class);
    }
}

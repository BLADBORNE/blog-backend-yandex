package configuration.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.blog.backend.repository.PostRepository;
import ru.blog.backend.service.CommentService;
import ru.blog.backend.service.PostService;
import ru.blog.backend.service.TagService;
import ru.blog.backend.service.impl.PostServiceImpl;

@Configuration
@Import({MockPostRepositoryConfiguration.class, MockTagServiceConfiguration.class, MockCommentServiceConfiguration.class})
public class MockPostServiceConfiguration {

    @Bean
    public PostService initPostService(
        PostRepository postRepository,
        TagService tagService,
        CommentService commentService
    ) {
        return new PostServiceImpl(postRepository, tagService, commentService);
    }


}

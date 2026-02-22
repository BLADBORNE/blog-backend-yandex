package configuration.integration.web;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import ru.blog.backend.controller.PostController;
import ru.blog.backend.controller.error.BlogErrorHandler;
import ru.blog.backend.service.CommentService;
import ru.blog.backend.service.PostFileService;
import ru.blog.backend.service.PostService;

@Configuration
@EnableWebMvc
public class IntegrationWebConfiguration {

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public PostController initPostController(
        PostService postService,
        CommentService commentService,
        PostFileService postFileService
    ) {
        return new PostController(postService, commentService, postFileService);
    }

    @Bean
    public BlogErrorHandler initBlogErrorHandler() {
        return new BlogErrorHandler();
    }

    @Bean
    public Validator validator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor(Validator validator) {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(validator);
        return processor;
    }
}

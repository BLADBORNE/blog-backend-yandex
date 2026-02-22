package integration.web;

import com.jayway.jsonpath.JsonPath;
import configuration.integration.service.IntegrationServiceConfiguration;
import configuration.integration.web.IntegrationWebConfiguration;
import integration.AbstractIntegrationTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Path;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebAppConfiguration
@ContextHierarchy({
    @ContextConfiguration(name = "service", classes = IntegrationServiceConfiguration.class),
    @ContextConfiguration(name = "web", classes = IntegrationWebConfiguration.class)
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class IntegrationPostControllerTest extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @TempDir
    private static Path TEMP_DIR;

    @DynamicPropertySource
    private static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("file.dir", TEMP_DIR::toString);
    }

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void shouldCreatePost() throws Exception {
        String json = """
                {
                    "title": "Новый пост",
                    "text": "Текст поста",
                    "tags": ["spring", "java"]
                }
            """;

        mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Новый пост"))
            .andExpect(jsonPath("$.text").value("Текст поста"))
            .andExpect(jsonPath("$.tags", hasSize(2)))
            .andExpect(jsonPath("$.tags", containsInAnyOrder("spring", "java")));
    }

    @Test
    void shouldUpdatePost() throws Exception {
        Long postId = createPost("Первоначальный пост");
        String updateJson = """
                {
                    "id": %d,
                    "title": "Обновлённый пост",
                    "text": "Новый текст",
                    "tags": ["update"]
                }
            """.formatted(postId);

        mockMvc.perform(put("/posts/" + postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Обновлённый пост"))
            .andExpect(jsonPath("$.tags", hasSize(1)))
            .andExpect(jsonPath("$.tags[0]").value("update"));
    }

    @Test
    void shouldDeletePost() throws Exception {
        Long postId = createPost("Пост для удаления");
        mockMvc.perform(delete("/posts/" + postId))
            .andExpect(status().isOk());
        mockMvc.perform(get("/posts/" + postId))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldGetPostById() throws Exception {
        Long postId = createPost("Пост для получения");
        mockMvc.perform(get("/posts/" + postId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(postId.intValue()));
    }

    @Test
    void shouldIncreaseLike() throws Exception {
        Long postId = createPost("Пост для лайка");
        mockMvc.perform(post("/posts/" + postId + "/likes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNumber());
    }

    @Test
    void shouldAddComment() throws Exception {
        Long postId = createPost("Пост с комментарием");

        String commentJson = """
                {
                    "postId": %d,
                    "text": "Новый комментарий"
                }
            """.formatted(postId);

        mockMvc.perform(post("/posts/" + postId + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(commentJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.text").value("Новый комментарий"))
            .andExpect(jsonPath("$.postId").value(postId.intValue()));
    }

    @Test
    void shouldGetCommentsByPostId() throws Exception {
        Long postId = createPost("Пост с комментариями");
        String commentJson = """
                {
                    "postId": %d,
                    "text": "Первый комментарий"
                }
            """.formatted(postId);
        mockMvc.perform(post("/posts/" + postId + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(commentJson))
            .andExpect(status().isOk());
        mockMvc.perform(get("/posts/" + postId + "/comments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldUploadAndDownloadImage() throws Exception {
        Long postId = createPost("Пост с изображением");
        byte[] fileContent = new byte[]{1, 2, 3, 4};
        MockMultipartFile file = new MockMultipartFile("image", "image.png", "image/png", fileContent);
        mockMvc.perform(multipart("/posts/" + postId + "/image")
                .file(file)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
            )
            .andExpect(status().isOk());
        mockMvc.perform(get("/posts/" + postId + "/image"))
            .andExpect(status().isOk())
            .andExpect(content().bytes(fileContent));
    }

    @Test
    void shouldFailValidationOnEmptyTitle() throws Exception {
        String json = """
                {
                    "title": "",
                    "text": "Текст",
                    "tags": ["tag"]
                }
            """;
        mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    private Long createPost(String title) throws Exception {
        String json = """
                {
                    "title": "%s",
                    "text": "Текст поста",
                    "tags": ["spring", "java"]
                }
            """.formatted(title);
        String response = mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        Number id = JsonPath.read(response, "$.id");
        return id.longValue();
    }
}

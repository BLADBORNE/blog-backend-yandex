package ru.blog.backend.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.blog.backend.marker.SavePostMarker;

import java.util.List;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(force = true)
public class PostCreateRequestDto {

    @NotBlank(message = "Название поста не может быть пустым", groups = SavePostMarker.class)
    private final String title;

    @NotBlank(message = "Текст поста не может быть путсым", groups = SavePostMarker.class)
    private String text;

    @NotEmpty(message = "Список тегов поста не может быть пустым", groups = SavePostMarker.class)
    private List<@NotBlank(message = "Теги не могут быть пустыми") String> tags;
}

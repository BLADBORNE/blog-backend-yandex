package ru.yandex.blog.dto.post;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.yandex.blog.marker.SavePostMarker;

@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(force = true)
public class PostUpdateRequestDto extends PostCreateRequestDto {

    @NotNull(message = "Идентификатор поста не может быть пустым", groups = SavePostMarker.class)
    private final Long id;
}

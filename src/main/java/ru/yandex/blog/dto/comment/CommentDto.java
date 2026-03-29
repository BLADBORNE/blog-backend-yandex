package ru.yandex.blog.dto.comment;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.yandex.blog.marker.SaveCommentMarker;

@Getter
@SuperBuilder
@NoArgsConstructor(force = true)
public final class CommentDto extends CommentCreateRequestDto {

    @NotNull(message = "Идентификатор комментария не может быть пустым", groups = SaveCommentMarker.class)
    private final Long id;
}

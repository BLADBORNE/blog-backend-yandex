package ru.yandex.blog.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.yandex.blog.marker.SaveCommentMarker;

@Getter
@SuperBuilder
@NoArgsConstructor(force = true)
public class CommentCreateRequestDto {

    @NotNull(message = "Идентификатор поста не может быть пустым", groups = SaveCommentMarker.class)
    private final Long postId;

    @NotBlank(message = "Текст комментария не может быть пустым", groups = SaveCommentMarker.class)
    private final String text;
}

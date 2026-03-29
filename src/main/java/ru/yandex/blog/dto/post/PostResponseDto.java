package ru.yandex.blog.dto.post;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
public final class PostResponseDto extends PostCreateRequestDto {

    private final Long id;

    private final Long likesCount;

    private Long commentsCount;
}

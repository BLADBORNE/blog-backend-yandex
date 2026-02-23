package ru.blog.backend.dto.post;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import ru.blog.backend.page.PageResponse;

import java.util.Collection;
import java.util.Collections;

@Getter
@SuperBuilder(toBuilder = true)
public class PostPageResponseDto extends PageResponse {

    private final Collection<PostResponseDto> posts;

    public static PostPageResponseDto initResponse(
        Collection<PostResponseDto> value,
        Boolean hasPrev,
        Boolean hasNext,
        Integer lastPage
    ) {
        return PostPageResponseDto.builder()
            .posts(value)
            .hasPrev(hasPrev)
            .hasNext(hasNext)
            .lastPage(lastPage)
            .build();
    }

    public static PostPageResponseDto empty() {
        return PostPageResponseDto.builder()
            .posts(Collections.emptyList())
            .hasPrev(Boolean.FALSE)
            .hasNext(Boolean.FALSE)
            .lastPage(0)
            .build();
    }
}

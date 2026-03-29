package ru.yandex.blog.page;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
public class PageResponse {

    private final Boolean hasPrev;

    private final Boolean hasNext;

    private final Integer lastPage;
}

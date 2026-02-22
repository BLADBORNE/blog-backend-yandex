package ru.blog.backend.page;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PageableRequest(@NotNull @Positive Integer pageNumber, @NotNull @Positive Integer pageSize) {

}

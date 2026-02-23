package ru.blog.backend.model.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {

    /**
     * <p>Непредвиденная.</p>
     */
    INTERNAL_SERVER_ERROR("Произошла непредвиденная ошибка."),

    /**
     * <p>Объект не найден.</p>
     */
    NOT_FOUND("Запрашиваемый объект не был найден"),

    /**
     * <p>Ограничение целостности.</p>
     */
    CONFLICT("Было нарушено ограничение целостности"),

    /**
     * <p>Некорректный запрос.</p>
     */
    BAD_REQUEST("Запрос составлен неправильно"),

    /**
     * <p>Нет доступа.</p>
     */
    FORBIDDEN("Операция не разрешена"),

    /**
     * <p>Ресурс недоступен.</p>
     */
    GONE("Ресурс больше не доступен"),

    /**
     * <p>Ошибка при авторизации.</p>
     */
    UNAUTHORIZED("Не авторизован");

    /**
     * <p>Описание.</p>
     */
    private final String description;
}

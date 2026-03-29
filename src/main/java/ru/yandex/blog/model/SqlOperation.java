package ru.yandex.blog.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SqlOperation {

    SELECT_DATA("Получение данных"),

    SELECT_COUNT("Получение кол-ва данных для пагинации");

    private final String description;
}

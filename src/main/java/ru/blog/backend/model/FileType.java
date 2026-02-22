package ru.blog.backend.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@RequiredArgsConstructor
@Getter
public enum FileType {

    JPG(IMAGE_JPEG_VALUE),

    PNG(IMAGE_PNG_VALUE);

    private final String applicationValue;

    public static FileType parseType(String type) {

        return Arrays.stream(values())
            .filter(v -> v.getApplicationValue().equalsIgnoreCase(type))
            .findFirst()
            .orElse(null);
    }
}

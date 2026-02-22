package ru.blog.backend.dto.tag;

import java.util.List;

public record PostTagInsertDto(Long postId, List<Long> tagIds) {

}

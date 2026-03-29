package ru.yandex.blog.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.blog.dto.file.PostFileDto;
import ru.yandex.blog.repository.PostFileRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostFileRepositoryImpl implements PostFileRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public void savePostFileInfo(PostFileDto postFileDto) {
        String sql = "INSERT INTO blog.post_file_info (FILE_NAME, POST_ID) VALUES (:fileName, :postId)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("fileName", postFileDto.fileName());
        params.addValue("postId", postFileDto.postId());
        namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public void updatePostFileInfo(PostFileDto postFileDto) {
        String sql = "UPDATE blog.post_file_info SET FILE_NAME = :fileName WHERE POST_ID = :postId";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("fileName", postFileDto.fileName());
        params.addValue("postId", postFileDto.postId());

        namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public Optional<PostFileDto> findFileByPostId(Long postId) {
        String sql = "SELECT pf.POST_ID, pf.FILE_NAME FROM blog.post_file_info pf WHERE pf.POST_ID = :postId";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", postId);

        return namedParameterJdbcTemplate
            .query(sql, params, this::createPostFileResponse)
            .stream()
            .findFirst();
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT EXISTS (SELECT 1 FROM blog.post_file_info pf WHERE pf.POST_ID = :postId)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", id);

        return Boolean.TRUE.equals(namedParameterJdbcTemplate.queryForObject(sql, params, Boolean.class));
    }

    private PostFileDto createPostFileResponse(ResultSet rs, int rowNum) throws SQLException {
        return new PostFileDto(rs.getString("FILE_NAME"), rs.getLong("POST_ID"));
    }
}

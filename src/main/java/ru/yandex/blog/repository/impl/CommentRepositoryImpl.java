package ru.yandex.blog.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.blog.dto.comment.CommentCreateRequestDto;
import ru.yandex.blog.dto.comment.CommentDto;
import ru.yandex.blog.repository.CommentRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public CommentDto addCommentToPost(CommentCreateRequestDto requestDto) {
        String sql = """
            INSERT INTO blog.post_comment (POST_ID, TEXT) VALUES (:postId, :text)
            RETURNING ID, POST_ID, TEXT
            """;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", requestDto.getPostId());
        params.addValue("text", requestDto.getText());

        return namedParameterJdbcTemplate.queryForObject(sql, params, this::createResponseDto);
    }

    @Override
    public List<CommentDto> findCommentsByPostId(Long postId) {
        String sql = "SELECT ps.ID, ps.POST_ID, ps.TEXT FROM blog.post_comment ps WHERE ps.POST_ID = :postId";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", postId);

        return namedParameterJdbcTemplate.query(sql, params, this::createResponseDto);
    }

    @Override
    public Map<Long, Long> countCommentsByPostIds(Collection<Long> postIds) {
        String sql = """
            SELECT ps.POST_ID, COUNT(ps.POST_ID) AS COUNT_POST_COMMENTS
            FROM blog.post_comment ps
            WHERE ps.POST_ID IN (:postIds)
            GROUP BY ps.POST_ID
            """;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postIds", postIds);
        Map<Long, Long> results = new HashMap<>();
        namedParameterJdbcTemplate.query(
            sql,
            params,
            (ResultSet rs, int rowNum) -> results.put(rs.getLong("POST_ID"), rs.getLong("COUNT_POST_COMMENTS"))
        );

        return results;
    }

    @Override
    public CommentDto updatePostComment(CommentDto requestDto) {
        String sql = """
            UPDATE blog.post_comment ps SET TEXT = :text
            WHERE ps.ID = :commentId
            RETURNING ps.ID, ps.POST_ID, ps.TEXT
            """;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("text", requestDto.getText());
        params.addValue("commentId", requestDto.getId());

        return namedParameterJdbcTemplate.queryForObject(sql, params, this::createResponseDto);
    }

    @Override
    public Long countCommentsByPostId(Long postId) {
        String sql = "SELECT COUNT(ps.ID) FROM blog.post_comment ps WHERE ps.POST_ID = :postId";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", postId);

        return namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
    }

    @Override
    public Optional<CommentDto> findCommentById(Long commentId) {
        String sql = "SELECT ps.ID, ps.POST_ID, ps.TEXT FROM blog.post_comment ps WHERE ps.ID = :commentId";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("commentId", commentId);

        return namedParameterJdbcTemplate
            .query(sql, params, this::createResponseDto)
            .stream()
            .findFirst();
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT EXISTS (SELECT 1 FROM blog.post_comment ps WHERE ps.ID = :commentId)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("commentId", id);

        return Boolean.TRUE.equals(namedParameterJdbcTemplate.queryForObject(sql, params, Boolean.class));
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM blog.post_comment ps WHERE ps.ID = :commentId";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("commentId", id);
        namedParameterJdbcTemplate.update(sql, params);
    }

    private CommentDto createResponseDto(ResultSet rs, int rowNum) throws SQLException {
        return CommentDto.builder()
            .id(rs.getLong("ID"))
            .text(rs.getString("TEXT"))
            .postId(rs.getLong("POST_ID"))
            .build();
    }
}

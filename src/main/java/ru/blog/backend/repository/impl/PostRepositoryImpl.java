package ru.blog.backend.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.blog.backend.dto.post.PostResponseDto;
import ru.blog.backend.dto.post.PostCreateRequestDto;
import ru.blog.backend.dto.post.PostUpdateRequestDto;
import ru.blog.backend.model.SqlOperation;
import ru.blog.backend.page.PageableRequest;
import ru.blog.backend.repository.PostRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static ru.blog.backend.model.SqlOperation.SELECT_COUNT;
import static ru.blog.backend.model.SqlOperation.SELECT_DATA;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final Long DEFAULT_POST_COMMENTS = 0L;

    @Override
    public PostResponseDto save(PostCreateRequestDto request) {
        String sql = """
            INSERT INTO blog.post (TITLE, TEXT) VALUES (:title, :text)
            RETURNING ID, TITLE, TEXT, LIKES
            """;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("title", request.getTitle());
        params.addValue("text", request.getText());

        return namedParameterJdbcTemplate.queryForObject(sql, params, this::createResponse);
    }

    @Override
    public PostResponseDto update(PostUpdateRequestDto request) {
        String sql = """
            UPDATE blog.post SET TITLE = :title, TEXT = :text
            WHERE ID = :id
            RETURNING ID, TITLE, TEXT, LIKES
            """;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("title", request.getTitle());
        params.addValue("text", request.getText());
        params.addValue("id", request.getId());

        return namedParameterJdbcTemplate.queryForObject(sql, params, this::createResponse);
    }

    @Override
    public Integer increaseLike(Long postId) {
        String sql = "UPDATE blog.post SET LIKES = LIKES + 1 WHERE id = :postId RETURNING LIKES";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", postId);

        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public Optional<PostResponseDto> findById(Long id) {
        String sql = "SELECT p.ID, p.TITLE, p.TEXT, p.LIKES FROM blog.post p WHERE p.ID = :postId";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", id);

        return namedParameterJdbcTemplate.query(sql, params, this::createResponse).stream().findFirst();
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT EXISTS (SELECT 1 FROM blog.post p WHERE p.ID = :id)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        return Boolean.TRUE.equals(namedParameterJdbcTemplate.queryForObject(sql, params, Boolean.class));
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM blog.post p WHERE p.ID = :id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public Integer findTotalBySearch(SqlOperation sqlOperation, String[] tags, String[] words) {
        SearchCriteria criteria = getSearchCriteria(sqlOperation, tags, words, null);

        return namedParameterJdbcTemplate.queryForObject(criteria.sql, criteria.params, Integer.class);
    }

    @Override
    public List<PostResponseDto> findBySearch(
        SqlOperation sqlOperation,
        String[] tags,
        String[] words,
        PageableRequest pageableRequest
    ) {
        SearchCriteria criteria = getSearchCriteria(sqlOperation, tags, words, pageableRequest);

        return namedParameterJdbcTemplate.query(criteria.sql, criteria.params, this::createResponse);
    }

    private PostResponseDto createResponse(ResultSet rs, int rowNum) throws SQLException {
        return PostResponseDto.builder()
            .id(rs.getLong("ID"))
            .title(rs.getString("TITLE"))
            .text(rs.getString("TEXT"))
            .likesCount(rs.getLong("LIKES"))
            .commentsCount(DEFAULT_POST_COMMENTS)
            .build();
    }

    private SearchCriteria getSearchCriteria(
        SqlOperation sqlOperation,
        String[] tags,
        String[] words,
        PageableRequest pageableRequest
    ) {
        String sql = """
            %s FROM blog.post p
            JOIN blog.post_tag ps ON ps.POST_ID = p.ID
            JOIN blog.tag t ON t.ID = ps.TAG_ID
            WHERE (CARDINALITY(:tags) = 0 OR t.TITLE = ANY (:tags))
            AND (CARDINALITY(:words) = 0 OR EXISTS (SELECT 1 FROM UNNEST(:words) AS w WHERE p.TITLE ILIKE '%%' || w || '%%'))
            %s
            """.formatted(SELECT_COUNT.equals(sqlOperation)
                ? "SELECT COUNT(DISTINCT p.ID)"
                : "SELECT DISTINCT p.ID, p.TITLE, p.TEXT, p.LIKES",
            SELECT_COUNT.equals(sqlOperation)
                ? ""
                : "LIMIT :limit OFFSET :offset"
        );
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("tags", tags);
        params.addValue("words", words);

        if (SELECT_DATA.equals(sqlOperation)) {
            params.addValue("limit", pageableRequest.pageSize());
            params.addValue("offset", (pageableRequest.pageNumber() - 1) * pageableRequest.pageSize());
        }

        return new SearchCriteria(sql, params);
    }

    private record SearchCriteria(String sql, MapSqlParameterSource params) {

    }
}

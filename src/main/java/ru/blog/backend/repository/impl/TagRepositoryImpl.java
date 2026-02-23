package ru.blog.backend.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.blog.backend.dto.tag.PostTagDto;
import ru.blog.backend.dto.tag.PostTagInsertDto;
import ru.blog.backend.dto.tag.TagDto;
import ru.blog.backend.repository.TagRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

@Repository
@RequiredArgsConstructor
public class TagRepositoryImpl implements TagRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public List<TagDto> save(List<String> tags) {
        String sql = """
            WITH inserted_tags AS (
                INSERT INTO blog.tag (title)
                SELECT UNNEST(:tags)
                ON CONFLICT (title) DO NOTHING
                RETURNING ID, TITLE
            )
            SELECT ID, TITLE FROM inserted_tags
            UNION ALL
            SELECT t.id, t.title
            FROM blog.tag t
            WHERE t.title = ANY (:tags);
            """;

        return mapAndGetTags(sql, tags);
    }

    @Override
    public void saveTagsToPost(PostTagInsertDto postTagInsertDto) {
        String sql = """
            INSERT INTO blog.post_tag (POST_ID, TAG_ID)
            SELECT :postId, UNNEST(:tagIds)
            ON CONFLICT (POST_ID, TAG_ID) DO NOTHING
            """;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", postTagInsertDto.postId());
        params.addValue("tagIds", postTagInsertDto.tagIds().toArray(new Long[0]));
        namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public Map<Long, List<PostTagDto>> findTagsByPostIdIn(Collection<Long> postIds) {
        String sql = """
            SELECT ps.POST_ID, t.TITLE FROM blog.tag t
            JOIN blog.post_tag ps ON ps.TAG_ID = t.Id WHERE ps.POST_ID IN (:postIds)
            """;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postIds", postIds);

        return namedParameterJdbcTemplate.query(sql, params, this::createPostTagDto)
            .stream().collect(groupingBy(PostTagDto::postId));
    }

    @Override
    public void deleteTagsByPostId(Long postId) {
        String sql = "DELETE FROM blog.post_tag pt WHERE pt.POST_ID = :postId";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", postId);

        namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public List<TagDto> findByPostId(Long postId) {
        String sql = """
            SELECT t.ID, t.TITLE FROM blog.tag t
            JOIN blog.post_tag ps ON ps.TAG_ID = t.Id WHERE ps.POST_ID = :postId
            """;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", postId);

        return namedParameterJdbcTemplate.query(sql, params, this::createResponse);
    }

    private List<TagDto> mapAndGetTags(String sql, List<String> tags) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("tags", tags.toArray(new String[0]));

        return namedParameterJdbcTemplate.query(sql, params, this::createResponse);
    }

    private TagDto createResponse(ResultSet rs, int rowNum) throws SQLException {
        return new TagDto(rs.getLong("ID"), rs.getString("TITLE"));
    }

    private PostTagDto createPostTagDto(ResultSet rs, int rowNum) throws SQLException {
        return new PostTagDto(rs.getLong("POST_ID"), rs.getString("TITLE"));
    }
}

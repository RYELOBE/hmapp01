package com.campus.marketplace.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Statement;

@Repository
public class CircleCommentLikeRepository {

  private final JdbcTemplate jdbc;

  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("id", rs.getLong("id"));
    row.put("commentId", rs.getLong("comment_id"));
    row.put("userId", rs.getLong("user_id"));
    row.put("createTime", rs.getTimestamp("create_time") != null ? rs.getTimestamp("create_time").toString() : null);
    return row;
  };

  public CircleCommentLikeRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> save(Long commentId, Long userId) {
    KeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      var ps = con.prepareStatement(
          "INSERT INTO circle_comment_like (comment_id, user_id) VALUES (?, ?)",
          Statement.RETURN_GENERATED_KEYS);
      ps.setLong(1, commentId);
      ps.setLong(2, userId);
      return ps;
    }, kh);
    Long id = kh.getKey().longValue();
    return findById(id);
  }

  public Map<String, Object> findById(Long id) {
    List<Map<String, Object>> results = jdbc.query("SELECT * FROM circle_comment_like WHERE id = ?", ROW_MAPPER, id);
    return results.isEmpty() ? null : results.get(0);
  }

  public Optional<Map<String, Object>> findByCommentIdAndUserId(Long commentId, Long userId) {
    List<Map<String, Object>> results = jdbc.query(
        "SELECT * FROM circle_comment_like WHERE comment_id = ? AND user_id = ?", ROW_MAPPER, commentId, userId);
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }

  public int countByCommentId(Long commentId) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM circle_comment_like WHERE comment_id = ?", Integer.class, commentId);
    return count != null ? count : 0;
  }

  public void deleteByCommentIdAndUserId(Long commentId, Long userId) {
    jdbc.update("DELETE FROM circle_comment_like WHERE comment_id = ? AND user_id = ?", commentId, userId);
  }

  public void deleteById(Long id) {
    jdbc.update("DELETE FROM circle_comment_like WHERE id = ?", id);
  }
}

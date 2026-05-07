package com.campus.marketplace.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Statement;

@Repository
public class CircleCommentRepository {

  private final JdbcTemplate jdbc;

  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("id", rs.getLong("id"));
    row.put("postId", rs.getLong("post_id"));
    row.put("userId", rs.getLong("user_id"));
    row.put("content", rs.getString("content"));
    row.put("createTime", rs.getTimestamp("create_time") != null ? rs.getTimestamp("create_time").toString() : null);
    return row;
  };

  public CircleCommentRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> save(Long postId, Long userId, String content) {
    KeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      var ps = con.prepareStatement(
          "INSERT INTO circle_comment (post_id, user_id, content) VALUES (?, ?, ?)",
          Statement.RETURN_GENERATED_KEYS);
      ps.setLong(1, postId);
      ps.setLong(2, userId);
      ps.setString(3, content);
      return ps;
    }, kh);
    Long id = kh.getKey().longValue();
    return findById(id);
  }

  public Map<String, Object> findById(Long id) {
    List<Map<String, Object>> results = jdbc.query("SELECT * FROM circle_comment WHERE id = ?", ROW_MAPPER, id);
    return results.isEmpty() ? null : results.get(0);
  }

  public List<Map<String, Object>> findByPostIdOrderByCreateTimeAsc(Long postId, int pageNo, int pageSize) {
    String sql = "SELECT * FROM circle_comment WHERE post_id = ? ORDER BY create_time ASC LIMIT ? OFFSET ?";
    int offset = (pageNo - 1) * pageSize;
    return jdbc.query(sql, ROW_MAPPER, postId, pageSize, offset);
  }

  public int countByPostId(Long postId) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM circle_comment WHERE post_id = ?", Integer.class, postId);
    return count != null ? count : 0;
  }

  public void deleteByPostIdAndUserId(Long postId, Long userId) {
    jdbc.update("DELETE FROM circle_comment WHERE post_id = ? AND user_id = ?", postId, userId);
  }
}

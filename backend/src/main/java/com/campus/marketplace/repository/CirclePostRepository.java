package com.campus.marketplace.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
public class CirclePostRepository {

  private final JdbcTemplate jdbc;

  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("id", rs.getLong("id"));
    row.put("userId", rs.getLong("user_id"));
    row.put("title", rs.getString("title"));
    row.put("content", rs.getString("content"));
    row.put("images", rs.getString("images"));
    row.put("tags", rs.getString("tags"));
    row.put("status", rs.getString("status"));
    row.put("likeCount", rs.getInt("like_count"));
    row.put("commentCount", rs.getInt("comment_count"));
    row.put("viewCount", rs.getInt("view_count"));
    row.put("createTime", rs.getTimestamp("create_time") != null ? rs.getTimestamp("create_time").toString() : null);
    row.put("updateTime", rs.getTimestamp("update_time") != null ? rs.getTimestamp("update_time").toString() : null);
    return row;
  };

  public CirclePostRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> save(Long userId, String title, String content, String images, String tags) {
    KeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      var ps = con.prepareStatement(
          "INSERT INTO circle_post (user_id, title, content, images, tags, status, like_count, comment_count, view_count) VALUES (?, ?, ?, ?, ?, 'PENDING', 0, 0, 0)",
          Statement.RETURN_GENERATED_KEYS);
      ps.setLong(1, userId);
      ps.setString(2, title);
      ps.setString(3, content);
      ps.setString(4, images);
      ps.setString(5, tags);
      return ps;
    }, kh);
    Long id = kh.getKey().longValue();
    return findById(id);
  }

  public Map<String, Object> findById(Long id) {
    List<Map<String, Object>> results = jdbc.query("SELECT * FROM circle_post WHERE id = ?", ROW_MAPPER, id);
    return results.isEmpty() ? null : results.get(0);
  }

  public List<Map<String, Object>> findByStatus(String status, int pageNo, int pageSize) {
    String sql = "SELECT * FROM circle_post WHERE status = ? ORDER BY create_time DESC LIMIT ? OFFSET ?";
    int offset = (pageNo - 1) * pageSize;
    return jdbc.query(sql, ROW_MAPPER, status, pageSize, offset);
  }

  public List<Map<String, Object>> findByUserIdAndStatus(Long userId, String status, int pageNo, int pageSize) {
    String sql = "SELECT * FROM circle_post WHERE user_id = ? AND status = ? ORDER BY create_time DESC LIMIT ? OFFSET ?";
    int offset = (pageNo - 1) * pageSize;
    return jdbc.query(sql, ROW_MAPPER, userId, status, pageSize, offset);
  }

  public List<Map<String, Object>> findByTagAndStatus(String tag, String status, int pageNo, int pageSize) {
    String sql = "SELECT * FROM circle_post WHERE status = ? AND tags LIKE ? ORDER BY create_time DESC LIMIT ? OFFSET ?";
    int offset = (pageNo - 1) * pageSize;
    return jdbc.query(sql, ROW_MAPPER, status, "%" + tag + "%", pageSize, offset);
  }

  public int countByStatus(String status) {
    Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM circle_post WHERE status = ?", Integer.class, status);
    return count != null ? count : 0;
  }

  public int countAll() {
    Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM circle_post", Integer.class);
    return count != null ? count : 0;
  }

  public void delete(Long id) {
    jdbc.update("DELETE FROM circle_post WHERE id = ?", id);
  }

  public void updateStatus(Long id, String status) {
    jdbc.update("UPDATE circle_post SET status = ? WHERE id = ?", status, id);
  }

  public void incrementViewCount(Long id) {
    jdbc.update("UPDATE circle_post SET view_count = view_count + 1 WHERE id = ?", id);
  }

  public void incrementLikeCount(Long id) {
    jdbc.update("UPDATE circle_post SET like_count = like_count + 1 WHERE id = ?", id);
  }

  public void decrementLikeCount(Long id) {
    jdbc.update("UPDATE circle_post SET like_count = GREATEST(0, like_count - 1) WHERE id = ?", id);
  }

  public void incrementCommentCount(Long id) {
    jdbc.update("UPDATE circle_post SET comment_count = comment_count + 1 WHERE id = ?", id);
  }

  public int countByUserIdAndStatus(Long userId, String status) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM circle_post WHERE user_id = ? AND status = ?", Integer.class, userId, status);
    return count != null ? count : 0;
  }
}

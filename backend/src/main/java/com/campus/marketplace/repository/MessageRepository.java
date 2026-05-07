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
public class MessageRepository {

  private final JdbcTemplate jdbc;

  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("id", rs.getLong("id"));
    row.put("userId", rs.getLong("user_id"));
    row.put("type", rs.getString("type"));
    row.put("title", rs.getString("title"));
    row.put("content", rs.getString("content"));
    row.put("status", rs.getString("status"));
    row.put("link", rs.getString("link"));
    row.put("isDeleted", rs.getBoolean("is_deleted"));
    row.put("createTime", rs.getTimestamp("create_time") != null ? rs.getTimestamp("create_time").toString() : null);
    return row;
  };

  public MessageRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> save(Long userId, String type, String title, String content, String link) {
    KeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      var ps = con.prepareStatement(
          "INSERT INTO message (user_id, type, title, content, status, link, is_deleted) VALUES (?, ?, ?, ?, 'UNREAD', ?, false)",
          Statement.RETURN_GENERATED_KEYS);
      ps.setLong(1, userId);
      ps.setString(2, type);
      ps.setString(3, title);
      ps.setString(4, content);
      ps.setString(5, link);
      return ps;
    }, kh);
    Long id = kh.getKey().longValue();
    return findById(id);
  }

  public Map<String, Object> findById(Long id) {
    List<Map<String, Object>> results = jdbc.query("SELECT * FROM message WHERE id = ?", ROW_MAPPER, id);
    return results.isEmpty() ? null : results.get(0);
  }

  public List<Map<String, Object>> findByUserIdAndIsDeletedFalseOrderByCreateTimeDesc(Long userId, int pageNo, int pageSize) {
    String sql = "SELECT * FROM message WHERE user_id = ? AND is_deleted = false ORDER BY create_time DESC LIMIT ? OFFSET ?";
    int offset = (pageNo - 1) * pageSize;
    return jdbc.query(sql, ROW_MAPPER, userId, pageSize, offset);
  }

  public List<Map<String, Object>> findByUserIdAndTypeAndIsDeletedFalse(Long userId, String type, int pageNo, int pageSize) {
    String sql = "SELECT * FROM message WHERE user_id = ? AND type = ? AND is_deleted = false ORDER BY create_time DESC LIMIT ? OFFSET ?";
    int offset = (pageNo - 1) * pageSize;
    return jdbc.query(sql, ROW_MAPPER, userId, type, pageSize, offset);
  }

  public long countByUserIdAndStatusAndIsDeletedFalse(Long userId, String status) {
    Long count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM message WHERE user_id = ? AND status = ? AND is_deleted = false",
        Long.class, userId, status);
    return count != null ? count : 0;
  }

  public long countByUserIdAndIsDeletedFalse(Long userId) {
    Long count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM message WHERE user_id = ? AND is_deleted = false",
        Long.class, userId);
    return count != null ? count : 0;
  }

  public void updateStatus(Long id, String status) {
    jdbc.update("UPDATE message SET status = ? WHERE id = ?", status, id);
  }

  public List<Map<String, Object>> findUnreadByUserId(Long userId) {
    return jdbc.query(
        "SELECT * FROM message WHERE user_id = ? AND status = 'UNREAD' AND is_deleted = false ORDER BY create_time DESC",
        ROW_MAPPER, userId);
  }

  public void markAllAsReadByUserId(Long userId) {
    jdbc.update("UPDATE message SET status = 'READ' WHERE user_id = ? AND status = 'UNREAD'", userId);
  }

  public void softDelete(Long id) {
    jdbc.update("UPDATE message SET is_deleted = true WHERE id = ?", id);
  }
}

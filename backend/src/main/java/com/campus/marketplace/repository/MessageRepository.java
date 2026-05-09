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
    row.put("senderId", rs.getLong("sender_id"));
    row.put("senderName", rs.getString("sender_name"));
    row.put("receiverId", rs.getLong("receiver_id"));
    row.put("content", rs.getString("content"));
    row.put("type", rs.getString("type"));
    row.put("status", rs.getString("status"));
    row.put("createTime", rs.getTimestamp("create_time") != null ? rs.getTimestamp("create_time").toString() : null);
    return row;
  };

  public MessageRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> save(Long senderId, String senderName, Long receiverId, String content, String type) {
    KeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      var ps = con.prepareStatement(
          "INSERT INTO message (sender_id, sender_name, receiver_id, content, type, status) VALUES (?, ?, ?, ?, ?, 'UNREAD')",
          Statement.RETURN_GENERATED_KEYS);
      ps.setLong(1, senderId);
      ps.setString(2, senderName);
      ps.setLong(3, receiverId);
      ps.setString(4, content);
      ps.setString(5, type);
      return ps;
    }, kh);
    Long id = kh.getKey().longValue();
    return findById(id);
  }

  public Map<String, Object> findById(Long id) {
    List<Map<String, Object>> results = jdbc.query("SELECT * FROM message WHERE id = ?", ROW_MAPPER, id);
    return results.isEmpty() ? null : results.get(0);
  }

  public List<Map<String, Object>> findByReceiverId(Long receiverId, int pageNo, int pageSize) {
    String sql = "SELECT * FROM message WHERE receiver_id = ? ORDER BY create_time DESC LIMIT ? OFFSET ?";
    int offset = (pageNo - 1) * pageSize;
    return jdbc.query(sql, ROW_MAPPER, receiverId, pageSize, offset);
  }

  public List<Map<String, Object>> findByReceiverIdAndType(Long receiverId, String type, int pageNo, int pageSize) {
    String sql = "SELECT * FROM message WHERE receiver_id = ? AND type = ? ORDER BY create_time DESC LIMIT ? OFFSET ?";
    int offset = (pageNo - 1) * pageSize;
    return jdbc.query(sql, ROW_MAPPER, receiverId, type, pageSize, offset);
  }

  public long countByReceiverIdAndStatus(Long receiverId, String status) {
    Long count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM message WHERE receiver_id = ? AND status = ?",
        Long.class, receiverId, status);
    return count != null ? count : 0;
  }

  public long countByReceiverId(Long receiverId) {
    Long count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM message WHERE receiver_id = ?",
        Long.class, receiverId);
    return count != null ? count : 0;
  }

  public void updateStatus(Long id, String status) {
    jdbc.update("UPDATE message SET status = ? WHERE id = ?", status, id);
  }

  public List<Map<String, Object>> findUnreadByReceiverId(Long receiverId) {
    return jdbc.query(
        "SELECT * FROM message WHERE receiver_id = ? AND status = 'UNREAD' ORDER BY create_time DESC",
        ROW_MAPPER, receiverId);
  }

  public void markAsRead(Long id) {
    jdbc.update("UPDATE message SET status = 'READ' WHERE id = ?", id);
  }

  public void markAllAsReadByReceiverId(Long receiverId) {
    jdbc.update("UPDATE message SET status = 'READ' WHERE receiver_id = ? AND status = 'UNREAD'", receiverId);
  }

  public void deleteById(Long id) {
    jdbc.update("DELETE FROM message WHERE id = ?", id);
  }
}

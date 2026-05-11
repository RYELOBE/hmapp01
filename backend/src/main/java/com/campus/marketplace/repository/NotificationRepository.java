package com.campus.marketplace.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationRepository {

  private final JdbcTemplate jdbc;

  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("id", rs.getLong("id"));
    row.put("title", rs.getString("title"));
    row.put("content", rs.getString("content"));
    row.put("type", rs.getString("type"));
    row.put("isRead", rs.getBoolean("is_read"));
    row.put("receiverId", rs.getObject("receiver_id") != null ? rs.getLong("receiver_id") : null);
    row.put("senderId", rs.getObject("sender_id") != null ? rs.getLong("sender_id") : null);
    row.put("senderName", rs.getString("sender_name"));
    row.put("businessId", rs.getString("business_id"));
    row.put("businessType", rs.getString("business_type"));
    row.put("priority", rs.getString("priority"));
    row.put("status", rs.getString("status"));
    row.put("createdAt", rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toString() : null);
    row.put("readAt", rs.getTimestamp("read_at") != null ? rs.getTimestamp("read_at").toString() : null);
    return row;
  };

  public NotificationRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> save(String title, String content, String type, Long receiverId,
      Long senderId, String senderName, String businessId, String businessType, String priority) {
    jdbc.update(
        "INSERT INTO notification (title, content, type, receiver_id, sender_id, sender_name, business_id, business_type, priority, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE')",
        title, content, type, receiverId, senderId, senderName, businessId, businessType, priority);

    // 获取刚插入的记录
    List<Map<String, Object>> results = jdbc.query(
        "SELECT * FROM notification ORDER BY id DESC LIMIT 1", ROW_MAPPER);
    return results.isEmpty() ? null : results.get(0);
  }

  public Map<String, Object> findById(Long id) {
    String sql = "SELECT * FROM notification WHERE id = ? AND status = 'ACTIVE'";
    List<Map<String, Object>> results = jdbc.query(sql, ROW_MAPPER, id);
    return results.isEmpty() ? null : results.get(0);
  }

  public List<Map<String, Object>> findByReceiverId(Long receiverId, int limit) {
    String sql = "SELECT * FROM notification WHERE receiver_id = ? AND status = 'ACTIVE' ORDER BY created_at DESC LIMIT ?";
    return jdbc.query(sql, ROW_MAPPER, receiverId, limit);
  }

  public List<Map<String, Object>> findByReceiverIdPaged(Long receiverId, int page, int size) {
    int offset = (page - 1) * size;
    String sql = "SELECT * FROM notification WHERE receiver_id = ? AND status = 'ACTIVE' ORDER BY created_at DESC LIMIT ? OFFSET ?";
    return jdbc.query(sql, ROW_MAPPER, receiverId, size, offset);
  }

  public List<Map<String, Object>> findAll(int limit) {
    String sql = "SELECT * FROM notification WHERE status = 'ACTIVE' ORDER BY created_at DESC LIMIT ?";
    return jdbc.query(sql, ROW_MAPPER, limit);
  }

  public List<Map<String, Object>> findAllPaged(int page, int size) {
    int offset = (page - 1) * size;
    String sql = "SELECT * FROM notification WHERE status = 'ACTIVE' ORDER BY created_at DESC LIMIT ? OFFSET ?";
    return jdbc.query(sql, ROW_MAPPER, size, offset);
  }

  public List<Map<String, Object>> findUnreadByReceiverId(Long receiverId) {
    String sql = "SELECT * FROM notification WHERE receiver_id = ? AND is_read = FALSE AND status = 'ACTIVE' ORDER BY created_at DESC";
    return jdbc.query(sql, ROW_MAPPER, receiverId);
  }

  public int countUnreadByReceiverId(Long receiverId) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM notification WHERE receiver_id = ? AND is_read = FALSE AND status = 'ACTIVE'",
        Integer.class, receiverId);
    return count != null ? count : 0;
  }

  public int countByReceiverId(Long receiverId) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM notification WHERE receiver_id = ? AND status = 'ACTIVE'",
        Integer.class, receiverId);
    return count != null ? count : 0;
  }

  public int countAll() {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM notification WHERE status = 'ACTIVE'",
        Integer.class);
    return count != null ? count : 0;
  }

  public int countAllUnread() {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM notification WHERE is_read = FALSE AND status = 'ACTIVE'",
        Integer.class);
    return count != null ? count : 0;
  }

  public void markAsRead(Long id) {
    jdbc.update("UPDATE notification SET is_read = TRUE, read_at = NOW() WHERE id = ?", id);
  }

  public void markAllAsRead(Long receiverId) {
    if (receiverId != null) {
      jdbc.update("UPDATE notification SET is_read = TRUE, read_at = NOW() WHERE receiver_id = ? AND is_read = FALSE",
          receiverId);
    } else {
      jdbc.update("UPDATE notification SET is_read = TRUE, read_at = NOW() WHERE is_read = FALSE");
    }
  }

  public void softDelete(Long id) {
    jdbc.update("UPDATE notification SET status = 'DELETED' WHERE id = ?", id);
  }
}

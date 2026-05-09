package com.campus.marketplace.repository;

import com.campus.marketplace.entity.Notification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class NotificationRepository {

  private final JdbcTemplate jdbc;

  private static final RowMapper<Notification> ROW_MAPPER = (rs, rowNum) -> {
    Notification n = new Notification();
    n.setId(rs.getLong("id"));
    n.setTitle(rs.getString("title"));
    n.setContent(rs.getString("content"));
    n.setType(rs.getString("type"));
    n.setIsRead(rs.getBoolean("is_read"));

    Object receiverId = rs.getObject("receiver_id");
    n.setReceiverId(receiverId == null ? null : ((Number) receiverId).longValue());

    Object senderId = rs.getObject("sender_id");
    n.setSenderId(senderId == null ? null : ((Number) senderId).longValue());

    n.setSenderName(rs.getString("sender_name"));
    n.setBusinessId(rs.getString("business_id"));
    n.setBusinessType(rs.getString("business_type"));
    n.setPriority(rs.getString("priority"));
    n.setStatus(rs.getString("status"));

    Timestamp createdAt = rs.getTimestamp("created_at");
    n.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());

    Timestamp readAt = rs.getTimestamp("read_at");
    n.setReadAt(readAt == null ? null : readAt.toLocalDateTime());

    Timestamp updatedAt = rs.getTimestamp("updated_at");
    n.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());

    n.setDeleted(rs.getInt("deleted"));
    return n;
  };

  public NotificationRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Notification> findRecent(int limit) {
    return jdbc.query(
        "SELECT * FROM notification WHERE status = 'ACTIVE' AND deleted = 0 ORDER BY created_at DESC LIMIT ?",
        ROW_MAPPER,
        limit);
  }

  public long countUnread() {
    Long count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM notification WHERE is_read = false AND status = 'ACTIVE' AND deleted = 0",
        Long.class);
    return count == null ? 0 : count;
  }

  public List<Notification> query(String type, Boolean unread, String priority, String keyword, int page, int size) {
    StringBuilder sql = new StringBuilder("SELECT * FROM notification WHERE status = 'ACTIVE' AND deleted = 0");
    Map<String, Object> params = new LinkedHashMap<>();

    if (type != null && !type.isBlank()) {
      sql.append(" AND type = ?");
      params.put("p" + params.size(), type);
    }
    if (unread != null) {
      sql.append(" AND is_read = ?");
      params.put("p" + params.size(), !unread);
    }
    if (priority != null && !priority.isBlank()) {
      sql.append(" AND priority = ?");
      params.put("p" + params.size(), priority);
    }
    if (keyword != null && !keyword.isBlank()) {
      sql.append(" AND (title LIKE ? OR content LIKE ?)");
      String like = "%" + keyword + "%";
      params.put("p" + params.size(), like);
      params.put("p" + params.size(), like);
    }

    sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
    params.put("p" + params.size(), size);
    params.put("p" + params.size(), (page - 1) * size);

    Object[] args = params.values().toArray();
    return jdbc.query(sql.toString(), ROW_MAPPER, args);
  }

  public void markRead(Long id) {
    jdbc.update(
        "UPDATE notification SET is_read = true, read_at = NOW() WHERE id = ?",
        id);
  }

  public void markReadBatch(List<Long> ids) {
    if (ids == null || ids.isEmpty()) return;
    String inSql = String.join(",", ids.stream().map(x -> "?").toList());
    jdbc.update(
        "UPDATE notification SET is_read = true, read_at = NOW() WHERE id IN (" + inSql + ")",
        ids.toArray());
  }

  public void markAllRead() {
    jdbc.update(
        "UPDATE notification SET is_read = true, read_at = NOW() WHERE is_read = false AND status = 'ACTIVE' AND deleted = 0");
  }

  public void softDelete(Long id) {
    jdbc.update(
        "UPDATE notification SET status = 'DELETED', updated_at = NOW() WHERE id = ?",
        id);
  }

  public Long insert(Notification n) {
    KeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      var ps = con.prepareStatement(
          "INSERT INTO notification (title, content, type, is_read, receiver_id, sender_id, sender_name, business_id, business_type, priority, status, created_at, updated_at, deleted) "
              + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), 0)",
          Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, n.getTitle());
      ps.setString(2, n.getContent());
      ps.setString(3, n.getType());
      ps.setBoolean(4, n.getIsRead() != null && n.getIsRead());
      ps.setObject(5, n.getReceiverId());
      ps.setObject(6, n.getSenderId());
      ps.setString(7, n.getSenderName());
      ps.setString(8, n.getBusinessId());
      ps.setString(9, n.getBusinessType());
      ps.setString(10, n.getPriority());
      ps.setString(11, n.getStatus());
      return ps;
    }, kh);

    Number key = kh.getKey();
    return key == null ? null : key.longValue();
  }

  public void createBusinessNotification(String businessType, String businessId, String title, String content, Long receiverId) {
    Notification n = new Notification();
    n.setTitle(title);
    n.setContent(content);
    n.setType("BUSINESS");
    n.setBusinessType(businessType);
    n.setBusinessId(businessId);
    n.setReceiverId(receiverId);
    n.setPriority("MEDIUM");
    n.setIsRead(false);
    n.setStatus("ACTIVE");
    n.setCreatedAt(LocalDateTime.now());
    n.setUpdatedAt(LocalDateTime.now());
    insert(n);
  }

  public long count(String type, Boolean unread, String priority, String keyword) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM notification WHERE status = 'ACTIVE' AND deleted = 0");
    Map<String, Object> params = new LinkedHashMap<>();

    if (type != null && !type.isBlank()) {
      sql.append(" AND type = ?");
      params.put("p" + params.size(), type);
    }
    if (unread != null) {
      sql.append(" AND is_read = ?");
      params.put("p" + params.size(), !unread);
    }
    if (priority != null && !priority.isBlank()) {
      sql.append(" AND priority = ?");
      params.put("p" + params.size(), priority);
    }
    if (keyword != null && !keyword.isBlank()) {
      sql.append(" AND (title LIKE ? OR content LIKE ?)");
      String like = "%" + keyword + "%";
      params.put("p" + params.size(), like);
      params.put("p" + params.size(), like);
    }

    Object[] args = params.values().toArray();
    Long count = jdbc.queryForObject(sql.toString(), Long.class, args);
    return count != null ? count : 0;
  }
}

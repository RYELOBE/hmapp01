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
public class ReviewRepository {

  private final JdbcTemplate jdbc;

  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("id", rs.getLong("id"));
    row.put("orderId", rs.getLong("order_id"));
    row.put("itemId", rs.getLong("item_id"));
    row.put("buyerId", rs.getLong("buyer_id"));
    row.put("sellerId", rs.getLong("seller_id"));
    row.put("rating", rs.getInt("rating"));
    row.put("content", rs.getString("content"));
    row.put("images", rs.getString("images"));
    row.put("status", rs.getString("status"));
    row.put("replyContent", rs.getString("reply_content"));
    row.put("replyTime", rs.getTimestamp("reply_time") != null ? rs.getTimestamp("reply_time").toString() : null);
    row.put("createTime", rs.getTimestamp("create_time") != null ? rs.getTimestamp("create_time").toString() : null);
    return row;
  };

  public ReviewRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> create(Long orderId, Long itemId, Long buyerId, Long sellerId, int rating, String content, String images) {
    KeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      var ps = con.prepareStatement(
          "INSERT INTO review (order_id, item_id, buyer_id, seller_id, rating, content, images, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING')",
          Statement.RETURN_GENERATED_KEYS);
      ps.setLong(1, orderId);
      ps.setLong(2, itemId);
      ps.setLong(3, buyerId);
      ps.setLong(4, sellerId);
      ps.setInt(5, rating);
      ps.setString(6, content);
      ps.setString(7, images);
      return ps;
    }, kh);
    Long id = kh.getKey().longValue();
    return findById(id).orElseThrow();
  }

  public Optional<Map<String, Object>> findById(Long id) {
    List<Map<String, Object>> results = jdbc.query(
        "SELECT * FROM review WHERE id = ?", ROW_MAPPER, id);
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }

  public List<Map<String, Object>> findByItemId(Long itemId) {
    return jdbc.query(
        "SELECT * FROM review WHERE item_id = ? ORDER BY created_at DESC",
        ROW_MAPPER, itemId);
  }

  public Optional<Map<String, Object>> findByOrderId(Long orderId) {
    List<Map<String, Object>> results = jdbc.query(
        "SELECT * FROM review WHERE order_id = ?", ROW_MAPPER, orderId);
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }

  public List<Map<String, Object>> findByBuyerId(Long buyerId) {
    return jdbc.query(
        "SELECT * FROM review WHERE buyer_id = ? ORDER BY created_at DESC",
        ROW_MAPPER, buyerId);
  }

  public void updateReply(Long id, String replyContent) {
    jdbc.update(
        "UPDATE review SET reply_content = ?, reply_time = NOW() WHERE id = ?",
        replyContent, id);
  }

  public List<Map<String, Object>> findByItemIdAndStatus(Long itemId, String status) {
    return jdbc.query(
        "SELECT * FROM review WHERE item_id = ? AND status = ? ORDER BY create_time DESC",
        ROW_MAPPER, itemId, status);
  }

  public List<Map<String, Object>> findByStatus(String status) {
    return jdbc.query(
        "SELECT * FROM review WHERE status = ? ORDER BY create_time ASC",
        ROW_MAPPER, status);
  }

  public int countByStatus(String status) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM review WHERE status = ?", Integer.class, status);
    return count != null ? count : 0;
  }

  public void updateStatus(Long id, String status, String replyContent) {
    if (replyContent != null && !replyContent.isEmpty()) {
      jdbc.update(
          "UPDATE review SET status = ?, reply_content = ?, reply_time = NOW() WHERE id = ?",
          status, replyContent, id);
    } else {
      jdbc.update(
          "UPDATE review SET status = ? WHERE id = ?",
          status, id);
    }
  }

  public boolean existsByOrderId(Long orderId) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM review WHERE order_id = ?", Integer.class, orderId);
    return count != null && count > 0;
  }

  public int countByItemId(Long itemId) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM review WHERE item_id = ?", Integer.class, itemId);
    return count != null ? count : 0;
  }

  public Double averageRatingByItemId(Long itemId) {
    Double avg = jdbc.queryForObject(
        "SELECT AVG(rating) FROM review WHERE item_id = ?", Double.class, itemId);
    return avg;
  }

  public Map<Integer, Integer> getRatingDistribution(Long itemId) {
    List<Map<String, Object>> results = jdbc.queryForList(
        "SELECT rating, COUNT(*) as count FROM review WHERE item_id = ? GROUP BY rating",
        itemId);
    Map<Integer, Integer> distribution = new HashMap<>();
    for (int i = 1; i <= 5; i++) {
      distribution.put(i, 0);
    }
    for (Map<String, Object> row : results) {
      distribution.put(((Number) row.get("rating")).intValue(), ((Number) row.get("count")).intValue());
    }
    return distribution;
  }
}

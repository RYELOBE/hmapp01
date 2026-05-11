package com.campus.marketplace.repository;

import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ReviewRepository {

  private final JdbcTemplate jdbc;

  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("id", rs.getLong("id"));
    Object orderIdObj = rs.getObject("order_id");
    row.put("orderId", orderIdObj != null ? ((Number) orderIdObj).longValue() : null);
    row.put("itemId", rs.getLong("item_id"));
    row.put("buyerId", rs.getLong("buyer_id"));
    Object sellerIdObj = rs.getObject("seller_id");
    row.put("sellerId", sellerIdObj != null ? ((Number) sellerIdObj).longValue() : null);
    row.put("rating", rs.getInt("rating"));
    row.put("content", rs.getString("content"));
    row.put("images", rs.getString("images"));
    row.put("replyContent", rs.getString("reply"));
    row.put("replyTime", rs.getTimestamp("reply_time") != null ? rs.getTimestamp("reply_time").toString() : null);
    row.put("status", rs.getString("status"));
    row.put("createdAt", rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toString() : null);
    return row;
  };

  public ReviewRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> create(Long orderId, Long itemId, Long buyerId, Long sellerId, int rating, String content, String images) {
    KeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      var ps = con.prepareStatement(
          "INSERT INTO review (order_id, item_id, buyer_id, seller_id, rating, content, images, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
          Statement.RETURN_GENERATED_KEYS);
      ps.setLong(1, orderId);
      ps.setLong(2, itemId);
      ps.setLong(3, buyerId);
      if (sellerId != null) {
        ps.setLong(4, sellerId);
      } else {
        ps.setNull(4, Types.BIGINT);
      }
      ps.setInt(5, rating);
      ps.setString(6, content);
      ps.setString(7, images);
      ps.setString(8, "PENDING");
      return ps;
    }, kh);
    Long id = kh.getKey().longValue();
    return findById(id).orElseThrow();
  }

  /** 创建评价（支持 orderId 为 null） */
  public Map<String, Object> createWithNullableOrder(Long orderId, Long itemId, Long buyerId, Long sellerId, int rating, String content, String images) {
    KeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      var ps = con.prepareStatement(
          "INSERT INTO review (order_id, item_id, buyer_id, seller_id, rating, content, images, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
          Statement.RETURN_GENERATED_KEYS);
      if (orderId != null) {
        ps.setLong(1, orderId);
      } else {
        ps.setNull(1, Types.BIGINT);
      }
      ps.setLong(2, itemId);
      ps.setLong(3, buyerId);
      if (sellerId != null) {
        ps.setLong(4, sellerId);
      } else {
        ps.setNull(4, Types.BIGINT);
      }
      ps.setInt(5, rating);
      ps.setString(6, content);
      ps.setString(7, images);
      ps.setString(8, "PENDING");
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
        "UPDATE review SET reply = ?, reply_time = NOW() WHERE id = ?",
        replyContent, id);
  }

  public void updateStatus(Long id, String status) {
    jdbc.update("UPDATE review SET status = ? WHERE id = ?", status, id);
  }

  public List<Map<String, Object>> findByItemIdPaged(Long itemId, int page, int pageSize) {
    int offset = (page - 1) * pageSize;
    return jdbc.query(
        "SELECT * FROM review WHERE item_id = ? AND status = 'APPROVED' ORDER BY created_at DESC LIMIT ? OFFSET ?",
        ROW_MAPPER, itemId, pageSize, offset);
  }

  /** 按商品ID和状态分页查询评价 */
  public List<Map<String, Object>> findByItemIdAndStatusPaged(Long itemId, String status, int page, int pageSize) {
    int offset = (page - 1) * pageSize;
    return jdbc.query(
        "SELECT * FROM review WHERE item_id = ? AND status = ? ORDER BY created_at DESC LIMIT ? OFFSET ?",
        ROW_MAPPER, itemId, status, pageSize, offset);
  }

  public List<Map<String, Object>> findPendingPaged(int page, int pageSize) {
    int offset = (page - 1) * pageSize;
    return jdbc.query(
        "SELECT * FROM review WHERE status = 'PENDING' ORDER BY created_at DESC LIMIT ? OFFSET ?",
        ROW_MAPPER, pageSize, offset);
  }

  public int countByItemId(Long itemId) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM review WHERE item_id = ? AND status = 'APPROVED'", Integer.class, itemId);
    return count != null ? count : 0;
  }

  public int countByItemIdAndStatus(Long itemId, String status) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM review WHERE item_id = ? AND status = ?", Integer.class, itemId, status);
    return count != null ? count : 0;
  }

  public int countPending() {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM review WHERE status = 'PENDING'", Integer.class);
    return count != null ? count : 0;
  }

  /** 按状态分页查询 */
  public List<Map<String, Object>> findByStatusPaged(String status, int page, int pageSize) {
    int offset = (page - 1) * pageSize;
    return jdbc.query(
        "SELECT * FROM review WHERE status = ? ORDER BY created_at DESC LIMIT ? OFFSET ?",
        ROW_MAPPER, status, pageSize, offset);
  }

  /** 查询所有评价（分页） */
  public List<Map<String, Object>> findAllPaged(int page, int pageSize) {
    int offset = (page - 1) * pageSize;
    return jdbc.query(
        "SELECT * FROM review ORDER BY created_at DESC LIMIT ? OFFSET ?",
        ROW_MAPPER, pageSize, offset);
  }

  /** 按状态统计数量 */
  public int countByStatus(String status) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM review WHERE status = ?", Integer.class, status);
    return count != null ? count : 0;
  }

  /** 按关键词和状态分页查询 */
  public List<Map<String, Object>> findByKeywordPaged(String keyword, String status, int page, int pageSize) {
    int offset = (page - 1) * pageSize;
    StringBuilder sql = new StringBuilder("SELECT * FROM review WHERE 1=1");
    List<Object> params = new java.util.ArrayList<>();

    if (keyword != null && !keyword.isEmpty()) {
      sql.append(" AND (content LIKE ? OR id = ?)");
      params.add("%" + keyword + "%");
      try {
        params.add(Long.parseLong(keyword));
      } catch (NumberFormatException e) {
        params.add(0L);
      }
    }

    if (status != null && !status.isEmpty()) {
      sql.append(" AND status = ?");
      params.add(status);
    }

    sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
    params.add(pageSize);
    params.add(offset);

    return jdbc.query(sql.toString(), ROW_MAPPER, params.toArray());
  }

  /** 按关键词和状态统计数量 */
  public int countByKeywordAndStatus(String keyword, String status) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM review WHERE 1=1");
    List<Object> params = new java.util.ArrayList<>();

    if (keyword != null && !keyword.isEmpty()) {
      sql.append(" AND (content LIKE ? OR id = ?)");
      params.add("%" + keyword + "%");
      try {
        params.add(Long.parseLong(keyword));
      } catch (NumberFormatException e) {
        params.add(0L);
      }
    }

    if (status != null && !status.isEmpty()) {
      sql.append(" AND status = ?");
      params.add(status);
    }

    Integer count = jdbc.queryForObject(sql.toString(), Integer.class, params.toArray());
    return count != null ? count : 0;
  }

  /** 统计所有评价数量 */
  public int countAll() {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM review", Integer.class);
    return count != null ? count : 0;
  }

  public Double averageRatingByItemId(Long itemId) {
    Double avg = jdbc.queryForObject(
        "SELECT AVG(rating) FROM review WHERE item_id = ? AND status = 'APPROVED'", Double.class, itemId);
    return avg;
  }

  public Map<Integer, Integer> getRatingDistribution(Long itemId) {
    List<Map<String, Object>> results = jdbc.queryForList(
        "SELECT rating, COUNT(*) as count FROM review WHERE item_id = ? AND status = 'APPROVED' GROUP BY rating",
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

  public void deleteById(Long id) {
    jdbc.update("DELETE FROM review WHERE id = ?", id);
  }
}

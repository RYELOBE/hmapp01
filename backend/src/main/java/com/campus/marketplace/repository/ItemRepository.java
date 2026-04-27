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
public class ItemRepository {

  private final JdbcTemplate jdbc;
  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("id", rs.getLong("id"));
    row.put("title", rs.getString("title"));
    row.put("price", rs.getInt("price"));
    row.put("description", rs.getString("description"));
    row.put("sellerId", rs.getLong("seller_id"));
    row.put("sellerName", rs.getString("seller_name"));
    row.put("reviewStatus", rs.getString("review_status"));
    row.put("rejectReason", rs.getString("reject_reason"));
    return row;
  };

  public ItemRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> save(String title, Integer price, String description,
      Long sellerId, String sellerName) {
    KeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      var ps = con.prepareStatement(
          "INSERT INTO item (title, price, description, seller_id, seller_name, review_status) VALUES (?, ?, ?, ?, ?, 'PENDING')",
          Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, title);
      ps.setInt(2, price);
      ps.setString(3, description);
      ps.setLong(4, sellerId);
      ps.setString(5, sellerName);
      return ps;
    }, kh);
    Long id = kh.getKey().longValue();
    return findById(id);
  }

  public List<Map<String, Object>> findAll() {
    return jdbc.query("SELECT * FROM item ORDER BY id", ROW_MAPPER);
  }

  public List<Map<String, Object>> findApproved() {
    return jdbc.query("SELECT * FROM item WHERE review_status = 'APPROVED' ORDER BY id", ROW_MAPPER);
  }

  public List<Map<String, Object>> findBySellerId(Long sellerId) {
    return jdbc.query("SELECT * FROM item WHERE seller_id = ? ORDER BY id", ROW_MAPPER, sellerId);
  }

  public List<Map<String, Object>> findPending() {
    return jdbc.query("SELECT * FROM item WHERE review_status = 'PENDING' ORDER BY id", ROW_MAPPER);
  }

  public Map<String, Object> findById(Long id) {
    List<Map<String, Object>> results = jdbc.query("SELECT * FROM item WHERE id = ?", ROW_MAPPER, id);
    return results.isEmpty() ? null : results.get(0);
  }

  public void updateReviewStatus(Long id, String status, String reason) {
    jdbc.update("UPDATE item SET review_status = ?, reject_reason = ? WHERE id = ?", status, reason, id);
  }
}

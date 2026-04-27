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
public class OrderRepository {

  private final JdbcTemplate jdbc;
  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("id", rs.getLong("id"));
    row.put("itemId", rs.getLong("item_id"));
    row.put("itemTitle", rs.getString("item_title"));
    row.put("buyerId", rs.getLong("buyer_id"));
    row.put("sellerId", rs.getLong("seller_id"));
    row.put("amount", rs.getInt("amount"));
    row.put("status", rs.getString("status"));
    return row;
  };

  public OrderRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> save(Long buyerId, Long sellerId, Long itemId,
      String itemTitle, Integer amount) {
    KeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      var ps = con.prepareStatement(
          "INSERT INTO orders (item_id, item_title, buyer_id, seller_id, amount, status) VALUES (?, ?, ?, ?, ?, 'CREATED')",
          Statement.RETURN_GENERATED_KEYS);
      ps.setLong(1, itemId);
      ps.setString(2, itemTitle);
      ps.setLong(3, buyerId);
      ps.setLong(4, sellerId);
      ps.setInt(5, amount);
      return ps;
    }, kh);
    Long id = kh.getKey().longValue();
    List<Map<String, Object>> results = jdbc.query("SELECT * FROM orders WHERE id = ?", ROW_MAPPER, id);
    return results.isEmpty() ? null : results.get(0);
  }

  public List<Map<String, Object>> findAll() {
    return jdbc.query("SELECT * FROM orders ORDER BY id", ROW_MAPPER);
  }

  public List<Map<String, Object>> findByBuyerId(Long buyerId) {
    return jdbc.query("SELECT * FROM orders WHERE buyer_id = ? ORDER BY id", ROW_MAPPER, buyerId);
  }

  public List<Map<String, Object>> findBySellerId(Long sellerId) {
    return jdbc.query("SELECT * FROM orders WHERE seller_id = ? ORDER BY id", ROW_MAPPER, sellerId);
  }

  public void updateStatus(Long id, String status) {
    jdbc.update("UPDATE orders SET status = ? WHERE id = ?", status, id);
  }
}

package com.campus.marketplace.repository;

import java.util.ArrayList;
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
public class CartRepository {

  private final JdbcTemplate jdbc;

  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("id", rs.getLong("id"));
    row.put("userId", rs.getLong("user_id"));
    row.put("itemId", rs.getLong("item_id"));
    row.put("quantity", rs.getInt("quantity"));
    row.put("selected", rs.getBoolean("selected"));
    row.put("createTime", rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toString() : null);
    row.put("updateTime", rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toString() : null);
    return row;
  };

  public CartRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> create(Long userId, Long itemId, int quantity, boolean selected) {
    KeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      var ps = con.prepareStatement(
          "INSERT INTO cart (user_id, item_id, quantity, selected) VALUES (?, ?, ?, ?)",
          Statement.RETURN_GENERATED_KEYS);
      ps.setLong(1, userId);
      ps.setLong(2, itemId);
      ps.setInt(3, quantity);
      ps.setBoolean(4, selected);
      return ps;
    }, kh);
    Long id = kh.getKey().longValue();
    return findById(id).orElseThrow();
  }

  public Optional<Map<String, Object>> findById(Long id) {
    List<Map<String, Object>> results = jdbc.query(
        "SELECT * FROM cart WHERE id = ?", ROW_MAPPER, id);
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }

  public List<Map<String, Object>> findByUserId(Long userId) {
    return jdbc.query(
        "SELECT * FROM cart WHERE user_id = ? ORDER BY created_at DESC",
        ROW_MAPPER, userId);
  }

  public Optional<Map<String, Object>> findByUserIdAndItemId(Long userId, Long itemId) {
    List<Map<String, Object>> results = jdbc.query(
        "SELECT * FROM cart WHERE user_id = ? AND item_id = ?", ROW_MAPPER, userId, itemId);
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }

  public void updateQuantity(Long id, int quantity) {
    jdbc.update("UPDATE cart SET quantity = ? WHERE id = ?", quantity, id);
  }

  public void updateSelected(Long id, boolean selected) {
    jdbc.update("UPDATE cart SET selected = ? WHERE id = ?", selected, id);
  }

  public void updateSelectedByUserIdAndItemIds(Long userId, List<Long> itemIds, boolean selected) {
    if (itemIds == null || itemIds.isEmpty()) {
      return;
    }
    StringBuilder sql = new StringBuilder("UPDATE cart SET selected = ? WHERE user_id = ? AND id IN (");
    List<Object> params = new ArrayList<>();
    params.add(selected);
    params.add(userId);
    for (int i = 0; i < itemIds.size(); i++) {
      sql.append(i > 0 ? ",?" : "?");
      params.add(itemIds.get(i));
    }
    sql.append(")");
    jdbc.update(sql.toString(), params.toArray());
  }

  public void delete(Long id) {
    jdbc.update("DELETE FROM cart WHERE id = ?", id);
  }

  public void deleteByUserIdAndItemId(Long userId, Long itemId) {
    jdbc.update("DELETE FROM cart WHERE user_id = ? AND item_id = ?", userId, itemId);
  }

  public void clearByUserId(Long userId) {
    jdbc.update("DELETE FROM cart WHERE user_id = ?", userId);
  }

  public void clearSelectedByUserId(Long userId) {
    jdbc.update("DELETE FROM cart WHERE user_id = ? AND selected = true", userId);
  }

  public int countByUserId(Long userId) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM cart WHERE user_id = ?", Integer.class, userId);
    return count != null ? count : 0;
  }

  public int countSelectedByUserId(Long userId) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM cart WHERE user_id = ? AND selected = true", Integer.class, userId);
    return count != null ? count : 0;
  }
}

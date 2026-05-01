package com.campus.marketplace.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class FavoriteRepository {

  private final JdbcTemplate jdbc;

  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("id", rs.getLong("id"));
    row.put("userId", rs.getLong("user_id"));
    row.put("itemId", rs.getLong("item_id"));
    row.put("createTime", rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toString() : null);
    return row;
  };

  public FavoriteRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> create(Long userId, Long itemId) {
    jdbc.update(
        "INSERT INTO favorite (user_id, item_id) VALUES (?, ?)",
        userId, itemId);
    Optional<Map<String, Object>> result = findByUserIdAndItemId(userId, itemId);
    return result.orElseThrow();
  }

  public Optional<Map<String, Object>> findById(Long id) {
    List<Map<String, Object>> results = jdbc.query(
        "SELECT * FROM favorite WHERE id = ?", ROW_MAPPER, id);
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }

  public List<Map<String, Object>> findByUserId(Long userId) {
    return jdbc.query(
        "SELECT * FROM favorite WHERE user_id = ? ORDER BY created_at DESC",
        ROW_MAPPER, userId);
  }

  public Optional<Map<String, Object>> findByUserIdAndItemId(Long userId, Long itemId) {
    List<Map<String, Object>> results = jdbc.query(
        "SELECT * FROM favorite WHERE user_id = ? AND item_id = ?", ROW_MAPPER, userId, itemId);
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }

  public void delete(Long userId, Long itemId) {
    jdbc.update("DELETE FROM favorite WHERE user_id = ? AND item_id = ?", userId, itemId);
  }

  public boolean existsByUserIdAndItemId(Long userId, Long itemId) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM favorite WHERE user_id = ? AND item_id = ?", Integer.class, userId, itemId);
    return count != null && count > 0;
  }

  public int countByUserId(Long userId) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM favorite WHERE user_id = ?", Integer.class, userId);
    return count != null ? count : 0;
  }
}

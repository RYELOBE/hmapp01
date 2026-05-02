package com.campus.marketplace.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class FunctionRepository {

  private final JdbcTemplate jdbc;

  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("id", rs.getLong("id"));
    row.put("menuId", rs.getLong("menu_id"));
    row.put("functionCode", rs.getString("function_code"));
    row.put("functionName", rs.getString("function_name"));
    row.put("status", rs.getInt("status"));
    row.put("sortOrder", rs.getInt("sort_order"));
    row.put("createdAt", rs.getTimestamp("created_at"));
    row.put("updatedAt", rs.getTimestamp("updated_at"));
    return row;
  };

  public FunctionRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> findByMenuId(Long menuId) {
    return jdbc.query(
        "SELECT * FROM resource_function WHERE menu_id = ? ORDER BY sort_order, id",
        ROW_MAPPER, menuId);
  }

  public Map<String, Object> save(Map<String, Object> func) {
    Long id = func.get("id") != null ? ((Number) func.get("id")).longValue() : null;
    Long menuId = ((Number) func.get("menuId")).longValue();
    String functionCode = (String) func.get("functionCode");
    String functionName = (String) func.get("functionName");
    Integer status = func.get("status") != null ? ((Number) func.get("status")).intValue() : 1;
    Integer sortOrder = func.get("sortOrder") != null ? ((Number) func.get("sortOrder")).intValue() : 0;

    if (id == null) {
      jdbc.update("""
          INSERT INTO resource_function (menu_id, function_code, function_name, status, sort_order)
          VALUES (?, ?, ?, ?, ?)
          """, menuId, functionCode, functionName, status, sortOrder);
      try {
        return findById(findLastInsertId()).orElseGet(() -> {
          Map<String, Object> fallback = new HashMap<>();
          fallback.put("menuId", menuId);
          fallback.put("functionCode", functionCode);
          fallback.put("functionName", functionName);
          fallback.put("status", status);
          fallback.put("sortOrder", sortOrder);
          return fallback;
        });
      } catch (Exception e) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("menuId", menuId);
        fallback.put("functionCode", functionCode);
        fallback.put("functionName", functionName);
        fallback.put("status", status);
        fallback.put("sortOrder", sortOrder);
        return fallback;
      }
    } else {
      jdbc.update("""
          UPDATE resource_function SET function_code = ?, function_name = ?, status = ?, sort_order = ?
          WHERE id = ?
          """, functionCode, functionName, status, sortOrder, id);
      return findById(id).orElseThrow();
    }
  }

  public boolean delete(Long id) {
    int affected = jdbc.update("DELETE FROM resource_function WHERE id = ?", id);
    return affected > 0;
  }

  private Long findLastInsertId() {
    return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
  }

  public Optional<Map<String, Object>> findById(Long id) {
    List<Map<String, Object>> results = jdbc.query(
        "SELECT * FROM resource_function WHERE id = ?", ROW_MAPPER, id);
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }
}

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
public class ReviewRepository {

  private final JdbcTemplate jdbc;
  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("id", rs.getLong("id"));
    row.put("itemId", rs.getLong("item_id"));
    row.put("operatorId", rs.getLong("operator_id"));
    row.put("action", rs.getString("action"));
    row.put("reason", rs.getString("reason"));
    row.put("createdAt", rs.getString("created_at"));
    return row;
  };

  public ReviewRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> save(Long itemId, Long operatorId, String action, String reason) {
    KeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      var ps = con.prepareStatement(
          "INSERT INTO review_log (item_id, operator_id, action, reason, created_at) VALUES (?, ?, ?, ?, NOW())",
          Statement.RETURN_GENERATED_KEYS);
      ps.setLong(1, itemId);
      ps.setLong(2, operatorId);
      ps.setString(3, action);
      ps.setString(4, reason);
      return ps;
    }, kh);
    Long id = kh.getKey().longValue();
    List<Map<String, Object>> results = jdbc.query("SELECT * FROM review_log WHERE id = ?", ROW_MAPPER, id);
    return results.isEmpty() ? null : results.get(0);
  }

  public List<Map<String, Object>> findAll() {
    return jdbc.query("SELECT * FROM review_log ORDER BY id", ROW_MAPPER);
  }
}

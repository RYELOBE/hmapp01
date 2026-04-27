package com.campus.marketplace.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AIMessageRepository {

  private final JdbcTemplate jdbc;
  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("id", rs.getLong("id"));
    row.put("sessionId", rs.getString("session_id"));
    row.put("role", rs.getString("role"));
    row.put("content", rs.getString("content"));
    row.put("references", rs.getString("references_json"));
    row.put("createdAt", rs.getString("created_at"));
    return row;
  };

  public AIMessageRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public void save(String sessionId, String role, String content, String referencesJson) {
    jdbc.update(
        "INSERT INTO ai_message (session_id, role, content, references_json, created_at) VALUES (?, ?, ?, ?, NOW())",
        sessionId, role, content, referencesJson);
  }

  public List<Map<String, Object>> findBySessionId(String sessionId) {
    return jdbc.query(
        "SELECT * FROM ai_message WHERE session_id = ? ORDER BY id", ROW_MAPPER, sessionId);
  }
}

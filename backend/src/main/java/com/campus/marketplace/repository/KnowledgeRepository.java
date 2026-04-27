package com.campus.marketplace.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class KnowledgeRepository {

  private final JdbcTemplate jdbc;
  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("id", rs.getLong("id"));
    row.put("sourceType", rs.getString("source_type"));
    row.put("title", rs.getString("title"));
    row.put("content", rs.getString("content"));
    return row;
  };

  public KnowledgeRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> search(String query) {
    String normalized = query == null ? "" : ("%" + query.toLowerCase() + "%");
    return jdbc.query(
        "SELECT * FROM knowledge_chunk WHERE LOWER(content) LIKE ? OR LOWER(title) LIKE ? LIMIT 3",
        ROW_MAPPER, normalized, normalized);
  }

  public void add(String sourceType, String title, String content) {
    jdbc.update(
        "INSERT INTO knowledge_chunk (source_type, title, content) VALUES (?, ?, ?)",
        sourceType, title, content);
  }
}

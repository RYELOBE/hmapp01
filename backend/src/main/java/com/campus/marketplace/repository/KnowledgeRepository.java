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

  private static final RowMapper<Map<String, Object>> ITEM_ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("id", rs.getLong("id"));
    row.put("title", rs.getString("title"));
    row.put("price", rs.getInt("price"));
    row.put("description", rs.getString("description"));
    row.put("category", rs.getString("category"));
    row.put("conditionLevel", rs.getString("condition_level"));
    row.put("sellerName", rs.getString("seller_name"));
    row.put("sourceType", "item");
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

  /**
   * 搜索已审核商品（用于AI推荐）
   */
  public List<Map<String, Object>> searchItems(String query) {
    String keyword = "%" + (query == null ? "" : query.toLowerCase()) + "%";
    String sql = """
      SELECT id, title, price, description, category, condition_level, seller_name
      FROM item
      WHERE review_status = 'APPROVED'
      AND (LOWER(title) LIKE ? OR LOWER(description) LIKE ? OR LOWER(category) LIKE ?)
      ORDER BY created_at DESC
      LIMIT 5
      """;
    return jdbc.query(sql, ITEM_ROW_MAPPER, keyword, keyword, keyword);
  }

  /**
   * 获取热门商品（用于AI推荐）
   */
  public List<Map<String, Object>> getHotItems() {
    String sql = """
      SELECT i.id, i.title, i.price, i.description, i.category, i.condition_level, i.seller_name
      FROM item i
      LEFT JOIN item_stats s ON i.id = s.item_id
      WHERE i.review_status = 'APPROVED'
      ORDER BY COALESCE(s.view_count, 0) DESC, i.created_at DESC
      LIMIT 5
      """;
    return jdbc.query(sql, ITEM_ROW_MAPPER);
  }

  public void add(String sourceType, String title, String content) {
    jdbc.update(
        "INSERT INTO knowledge_chunk (source_type, title, content) VALUES (?, ?, ?)",
        sourceType, title, content);
  }
}

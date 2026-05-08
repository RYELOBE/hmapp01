package com.campus.marketplace.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ItemStatsRepository {

  private final NamedParameterJdbcTemplate jdbc;
  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> Map.of(
      "id", rs.getLong("id"),
      "itemId", rs.getLong("item_id"),
      "viewCount", rs.getInt("view_count"),
      "clickCount", rs.getInt("click_count"),
      "favoriteCount", rs.getInt("favorite_count"),
      "hotScore", rs.getBigDecimal("hot_score"));

  public ItemStatsRepository(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> findByItemId(Long itemId) {
    try {
      return jdbc.queryForObject(
          "SELECT * FROM item_stats WHERE item_id = :itemId",
          Map.of("itemId", itemId), ROW_MAPPER);
    } catch (Exception e) {
      return null;
    }
  }

  public List<Map<String, Object>> findHotItems(int limit) {
    return jdbc.query(
        "SELECT s.*, i.title, i.price, i.image_urls, i.seller_name, i.category, "
            + "i.condition_level, i.campus, i.created_at "
            + "FROM item_stats s JOIN item i ON s.item_id = i.id "
            + "WHERE i.review_status = 'APPROVED' "
            + "ORDER BY s.hot_score DESC LIMIT :limit",
        Map.of("limit", limit), (rs, rn) -> {
          Map<String, Object> map = new HashMap<>();
          map.put("id", rs.getLong("id"));
          map.put("itemId", rs.getLong("item_id"));
          map.put("title", rs.getString("title"));
          map.put("price", rs.getInt("price"));
          map.put("imageUrls", rs.getString("image_urls"));
          map.put("sellerName", rs.getString("seller_name"));
          map.put("category", rs.getString("category"));
          map.put("conditionLevel", rs.getString("condition_level"));
          map.put("campus", rs.getString("campus"));
          map.put("createdAt", rs.getTimestamp("created_at").toString());
          map.put("viewCount", rs.getInt("view_count"));
          map.put("clickCount", rs.getInt("click_count"));
          map.put("favoriteCount", rs.getInt("favorite_count"));
          map.put("hotScore", rs.getBigDecimal("hot_score"));
          return map;
        });
  }

  public void incrementView(Long itemId) {
    String sql = "INSERT INTO item_stats (item_id, view_count, hot_score) VALUES (:itemId, 1, 1.0) "
        + "ON DUPLICATE KEY UPDATE view_count = view_count + 1, hot_score = hot_score + 0.5";
    jdbc.update(sql, Map.of("itemId", itemId));
  }

  public void incrementClick(Long itemId) {
    String sql = "INSERT INTO item_stats (item_id, click_count, hot_score) VALUES (:itemId, 1, 2.0) "
        + "ON DUPLICATE KEY UPDATE click_count = click_count + 1, hot_score = hot_score + 2.0";
    jdbc.update(sql, Map.of("itemId", itemId));
  }

  public void incrementFavorite(Long itemId) {
    String sql = "INSERT INTO item_stats (item_id, favorite_count, hot_score) VALUES (:itemId, 1, 3.0) "
        + "ON DUPLICATE KEY UPDATE favorite_count = favorite_count + 1, hot_score = hot_score + 3.0";
    jdbc.update(sql, Map.of("itemId", itemId));
  }

  public void decrementFavorite(Long itemId) {
    jdbc.update(
        "UPDATE item_stats SET favorite_count = GREATEST(0, favorite_count - 1), "
            + "hot_score = GREATEST(0, hot_score - 3.0) WHERE item_id = :itemId",
        Map.of("itemId", itemId));
  }

  public void recalculateHotScore(Long itemId) {
    jdbc.update(
        "UPDATE item_stats SET hot_score = ROUND(click_count * 2.0 + favorite_count * 3.0 + view_count * 0.5, 2) "
            + "WHERE item_id = :itemId",
        Map.of("itemId", itemId));
  }

  public void initStatsForItem(Long itemId) {
    String sql = "INSERT IGNORE INTO item_stats (item_id, view_count, click_count, favorite_count, hot_score) "
        + "VALUES (:itemId, 0, 0, 0, 0.00)";
    jdbc.update(sql, Map.of("itemId", itemId));
  }

  public void batchInitWithRandomStats() {
    jdbc.update(
        "INSERT INTO item_stats (item_id, view_count, click_count, favorite_count, hot_score) "
            + "SELECT i.id, "
            + "FLOOR(50 + RAND() * 200), "
            + "FLOOR(20 + RAND() * 80), "
            + "FLOOR(5 + RAND() * 30), "
            + "ROUND((FLOOR(20 + RAND() * 80)) * 2.0 + (FLOOR(5 + RAND() * 30)) * 3.0 + (FLOOR(50 + RAND() * 200)) * 0.5, 2) "
            + "FROM item i LEFT JOIN item_stats s ON i.id = s.item_id "
            + "WHERE i.review_status = 'APPROVED' AND s.item_id IS NULL",
        new HashMap<>());
  }
}

package com.campus.marketplace.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    row.put("imageUrls", rs.getString("image_urls"));
    row.put("category", rs.getString("category"));
    row.put("conditionLevel", rs.getString("condition_level"));
    row.put("campus", rs.getString("campus"));
    row.put("createdAt", rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toString() : null);
    return row;
  };

  private static final RowMapper<Map<String, Object>> ROW_MAPPER_WITH_STATS = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("id", rs.getLong("id"));
    row.put("title", rs.getString("title"));
    row.put("price", rs.getInt("price"));
    row.put("description", rs.getString("description"));
    row.put("sellerId", rs.getLong("seller_id"));
    row.put("sellerName", rs.getString("seller_name"));
    row.put("reviewStatus", rs.getString("review_status"));
    row.put("rejectReason", rs.getString("reject_reason"));
    row.put("imageUrls", rs.getString("image_urls"));
    row.put("category", rs.getString("category"));
    row.put("conditionLevel", rs.getString("condition_level"));
    row.put("campus", rs.getString("campus"));
    row.put("createdAt", rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toString() : null);
    row.put("viewCount", rs.getInt("view_count"));
    row.put("clickCount", rs.getInt("click_count"));
    row.put("favoriteCount", rs.getInt("favorite_count"));
    return row;
  };

  private static final RowMapper<Map<String, Object>> DETAIL_ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = ROW_MAPPER_WITH_STATS.mapRow(rs, rowNum);
    row.put("sellerPhone", rs.getString("seller_phone"));
    row.put("sellerCampus", rs.getString("seller_campus"));
    row.put("sellerCreatedAt", rs.getTimestamp("seller_created_at") != null ? rs.getTimestamp("seller_created_at").toString() : null);
    row.put("sellerSoldCount", rs.getInt("seller_sold_count"));
    row.put("sellerRating", rs.getBigDecimal("seller_rating"));
    row.put("reviewCount", rs.getInt("review_count"));
    return row;
  };

  public ItemRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> save(String title, Integer price, String description,
      Long sellerId, String sellerName, String imageUrls, String category,
      String conditionLevel, String campus) {
    KeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      var ps = con.prepareStatement(
          "INSERT INTO item (title, price, description, seller_id, seller_name, review_status, image_urls, category, condition_level, campus) VALUES (?, ?, ?, ?, ?, 'PENDING_REVIEW', ?, ?, ?, ?)",
          Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, title);
      ps.setInt(2, price);
      ps.setString(3, description);
      ps.setLong(4, sellerId);
      ps.setString(5, sellerName);
      ps.setString(6, imageUrls);
      ps.setString(7, category);
      ps.setString(8, conditionLevel);
      ps.setString(9, campus);
      return ps;
    }, kh);
    Long id = kh.getKey().longValue();
    jdbc.update("INSERT IGNORE INTO item_stats (item_id, view_count, click_count, favorite_count, hot_score) VALUES (?, 0, 0, 0, 0.00)", id);
    return findById(id);
  }

  // 向后兼容
  public Map<String, Object> save(String title, Integer price, String description,
      Long sellerId, String sellerName) {
    return save(title, price, description, sellerId, sellerName, null, null, null, null);
  }

  public List<Map<String, Object>> findAll() {
    return jdbc.query("SELECT * FROM item ORDER BY created_at DESC, id DESC", ROW_MAPPER);
  }

  public List<Map<String, Object>> findApproved() {
    return jdbc.query("SELECT * FROM item WHERE review_status = 'APPROVED' ORDER BY created_at DESC, id DESC", ROW_MAPPER);
  }

  public List<Map<String, Object>> findBySellerId(Long sellerId) {
    return jdbc.query("SELECT * FROM item WHERE seller_id = ? ORDER BY created_at DESC, id DESC", ROW_MAPPER, sellerId);
  }

  public List<Map<String, Object>> findBySellerIdAndApproved(Long sellerId) {
    return jdbc.query("SELECT * FROM item WHERE seller_id = ? AND review_status = 'APPROVED' ORDER BY created_at DESC, id DESC", ROW_MAPPER, sellerId);
  }

  public List<Map<String, Object>> findPending() {
    return jdbc.query("SELECT * FROM item WHERE review_status = 'PENDING_REVIEW' ORDER BY created_at DESC, id DESC", ROW_MAPPER);
  }

  public Map<String, Object> findById(Long id) {
    List<Map<String, Object>> results = jdbc.query("SELECT * FROM item WHERE id = ?", ROW_MAPPER, id);
    return results.isEmpty() ? null : results.get(0);
  }

  public Map<String, Object> findDetailById(Long id) {
    String sql = """
        SELECT i.*, COALESCE(s.view_count, 0) AS view_count,
               COALESCE(s.click_count, 0) AS click_count,
               COALESCE(s.favorite_count, 0) AS favorite_count,
               u.phone AS seller_phone,
               u.campus AS seller_campus,
               u.created_at AS seller_created_at,
               (SELECT COUNT(*) FROM orders o WHERE o.seller_id = i.seller_id AND o.status = 'COMPLETED') AS seller_sold_count,
               (SELECT COALESCE(ROUND(AVG(r.rating), 1), 5.0) FROM review r WHERE r.seller_id = i.seller_id AND r.status = 'APPROVED') AS seller_rating,
               (SELECT COUNT(*) FROM review r WHERE r.item_id = i.id AND r.status = 'APPROVED') AS review_count
        FROM item i
        LEFT JOIN item_stats s ON i.id = s.item_id
        LEFT JOIN user_account u ON i.seller_id = u.id
        WHERE i.id = ?
        """;
    List<Map<String, Object>> results = jdbc.query(sql, DETAIL_ROW_MAPPER, id);
    return results.isEmpty() ? null : results.get(0);
  }

  public void updateReviewStatus(Long id, String status, String reason) {
    jdbc.update("UPDATE item SET review_status = ?, reject_reason = ? WHERE id = ?", status, reason, id);
  }

  public void update(Long id, String title, Integer price, String description, Object imageUrls,
      String category, String conditionLevel) {
    String imageUrlsStr = stringifyImageUrls(imageUrls);
    jdbc.update(
        "UPDATE item SET title = ?, price = ?, description = ?, image_urls = ?, category = ?, condition_level = ? WHERE id = ?",
        title, price, description, imageUrlsStr, category, conditionLevel, id);
  }

  public void delete(Long id) {
    jdbc.update("DELETE FROM item WHERE id = ?", id);
  }

  // ── 分页查询（支持筛选） ──────────────────────────
  public List<Map<String, Object>> findByPage(String status, String keyword, String category,
      int pageNo, int pageSize) {
    return findByPage(status, keyword, category, null, null, null, pageNo, pageSize);
  }

  public List<Map<String, Object>> findByPage(String status, String keyword, String category,
      String conditionLevel, String campus, String sort,
      int pageNo, int pageSize) {
    StringBuilder sql = new StringBuilder(
        "SELECT i.*, COALESCE(s.view_count, 0) AS view_count, " +
        "COALESCE(s.click_count, 0) AS click_count, COALESCE(s.favorite_count, 0) AS favorite_count " +
        "FROM item i LEFT JOIN item_stats s ON i.id = s.item_id WHERE 1=1");
    if (status != null && !status.isEmpty()) sql.append(" AND i.review_status = ?");
    if (keyword != null && !keyword.isEmpty()) sql.append(" AND i.title LIKE ?");
    if (category != null && !category.isEmpty()) sql.append(" AND i.category = ?");
    if (conditionLevel != null && !conditionLevel.isEmpty()) sql.append(" AND i.condition_level = ?");
    if (campus != null && !campus.isEmpty()) sql.append(" AND i.campus = ?");

    sql.append(buildOrderBy(sort));
    sql.append(" LIMIT ? OFFSET ?");

    List<Object> params = new ArrayList<>();
    if (status != null && !status.isEmpty()) params.add(status);
    if (keyword != null && !keyword.isEmpty()) params.add("%" + keyword + "%");
    if (category != null && !category.isEmpty()) params.add(category);
    if (conditionLevel != null && !conditionLevel.isEmpty()) params.add(conditionLevel);
    if (campus != null && !campus.isEmpty()) params.add(campus);
    params.add(pageSize);
    params.add((pageNo - 1) * pageSize);

    return jdbc.query(sql.toString(), ROW_MAPPER_WITH_STATS, params.toArray());
  }

  public int countByFilter(String status, String keyword, String category) {
    return countByFilter(status, keyword, category, null, null);
  }

  public int countByFilter(String status, String keyword, String category,
      String conditionLevel, String campus) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM item WHERE 1=1");
    if (status != null && !status.isEmpty()) sql.append(" AND review_status = ?");
    if (keyword != null && !keyword.isEmpty()) sql.append(" AND title LIKE ?");
    if (category != null && !category.isEmpty()) sql.append(" AND category = ?");
    if (conditionLevel != null && !conditionLevel.isEmpty()) sql.append(" AND condition_level = ?");
    if (campus != null && !campus.isEmpty()) sql.append(" AND campus = ?");

    List<Object> params = new ArrayList<>();
    if (status != null && !status.isEmpty()) params.add(status);
    if (keyword != null && !keyword.isEmpty()) params.add("%" + keyword + "%");
    if (category != null && !category.isEmpty()) params.add(category);
    if (conditionLevel != null && !conditionLevel.isEmpty()) params.add(conditionLevel);
    if (campus != null && !campus.isEmpty()) params.add(campus);

    Integer count = jdbc.queryForObject(sql.toString(), Integer.class, params.toArray());
    return count != null ? count : 0;
  }
  
  private String stringifyImageUrls(Object imageUrls) {
    if (imageUrls == null) {
      return null;
    }
    if (imageUrls instanceof List<?> list) {
      try {
        return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(list);
      } catch (Exception e) {
        return list.toString();
      }
    }
    return imageUrls.toString();
  }

  private String buildOrderBy(String sort) {
    if (sort == null || sort.isEmpty() || "latest".equals(sort)) {
      return " ORDER BY created_at DESC, id DESC";
    } else if ("price_asc".equals(sort)) {
      return " ORDER BY price ASC, id ASC";
    } else if ("price_desc".equals(sort)) {
      return " ORDER BY price DESC, id DESC";
    }
    return " ORDER BY created_at DESC, id DESC";
  }

  // ── 统计 ──────────────────────────
  public int countByStatus(String status) {
    Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM item WHERE review_status = ?", Integer.class, status);
    return count != null ? count : 0;
  }

  public int countAll() {
    Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM item", Integer.class);
    return count != null ? count : 0;
  }

  public int countBySellerId(Long sellerId) {
    Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM item WHERE seller_id = ?", Integer.class, sellerId);
    return count != null ? count : 0;
  }

  public int countBySellerIdAndStatus(Long sellerId, String status) {
    Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM item WHERE seller_id = ? AND review_status = ?", Integer.class, sellerId, status);
    return count != null ? count : 0;
  }

  /**
   * 按日期范围统计商品数量
   * @param status 商品状态
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @return 商品数量
   */
  public int countByStatusAndDateRange(String status, LocalDateTime startTime, LocalDateTime endTime) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM item WHERE created_at >= ? AND created_at < ?");
    List<Object> params = new ArrayList<>();
    params.add(java.sql.Timestamp.valueOf(startTime));
    params.add(java.sql.Timestamp.valueOf(endTime));

    if (status != null && !status.isEmpty()) {
      sql.append(" AND review_status = ?");
      params.add(status);
    }

    Integer count = jdbc.queryForObject(sql.toString(), Integer.class, params.toArray());
    return count != null ? count : 0;
  }

  /**
   * 统计今日发布商品数
   * @return 今日发布商品数量
   */
  public int countTodayItems() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);
    return countByStatusAndDateRange(null, startOfDay, endOfDay);
  }

  /**
   * 统计今日新增待审核商品数
   * @return 今日新增待审核商品数量
   */
  public int countTodayPendingItems() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);
    return countByStatusAndDateRange("PENDING_REVIEW", startOfDay, endOfDay);
  }

  /**
   * 统计今日审核通过商品数
   * @return 今日审核通过商品数量
   */
  public int countTodayApprovedItems() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);
    return countByStatusAndDateRange("APPROVED", startOfDay, endOfDay);
  }

  /**
   * 统计今日审核拒绝商品数
   * @return 今日审核拒绝商品数量
   */
  public int countTodayRejectedItems() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);
    return countByStatusAndDateRange("REJECTED", startOfDay, endOfDay);
  }

  /**
   * 按日期范围统计商品发布数量
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @return 商品数量
   */
  public int countItemsByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
    String sql = "SELECT COUNT(*) FROM item WHERE created_at >= ? AND created_at < ?";
    Integer count = jdbc.queryForObject(sql, Integer.class,
        java.sql.Timestamp.valueOf(startTime), java.sql.Timestamp.valueOf(endTime));
    return count != null ? count : 0;
  }

  /**
   * 按类别统计商品数量
   * @param category 商品类别
   * @return 商品数量
   */
  public int countByCategory(String category) {
    String sql = "SELECT COUNT(*) FROM item WHERE category = ? AND review_status = 'APPROVED'";
    Integer count = jdbc.queryForObject(sql, Integer.class, category);
    return count != null ? count : 0;
  }

  /**
   * 查询待审核商品列表（带分页）
   * @param pageNo 页码
   * @param pageSize 每页数量
   * @return 待审核商品列表
   */
  public List<Map<String, Object>> findPendingByPage(int pageNo, int pageSize) {
    String sql = "SELECT * FROM item WHERE review_status = 'PENDING_REVIEW' ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?";
    int offset = (pageNo - 1) * pageSize;
    return jdbc.query(sql, ROW_MAPPER, pageSize, offset);
  }

  /**
   * 统计待审核商品数量
   * @return 待审核商品数量
   */
  public int countPending() {
    return countByStatus("PENDING_REVIEW");
  }

  /** 卖家的商品分页（带状态筛选） */
  public List<Map<String, Object>> findBySellerIdPaged(Long sellerId, String status,
      int pageNo, int pageSize) {
    StringBuilder sql = new StringBuilder("SELECT * FROM item WHERE seller_id = ?");
    List<Object> params = new ArrayList<>();
    params.add(sellerId);
    if (status != null && !status.isEmpty()) {
      sql.append(" AND review_status = ?");
      params.add(status);
    }
    sql.append(" ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?");
    params.add(pageSize);
    params.add((pageNo - 1) * pageSize);
    return jdbc.query(sql.toString(), ROW_MAPPER, params.toArray());
  }

  public int countBySellerIdWithFilter(Long sellerId, String status) {
    if (status != null && !status.isEmpty()) {
      return countBySellerIdAndStatus(sellerId, status);
    }
    return countBySellerId(sellerId);
  }

  // ── 供方统计 ──────────────────────────
  public List<Map<String, Object>> findVendorsWithStats(String keyword, int pageNo, int pageSize) {
    String baseSql = "SELECT u.id, u.username, u.nickname, u.status, u.created_at AS createdAt " +
                     "FROM user_account u WHERE u.roles LIKE ? ";
    
    List<Object> params = new ArrayList<>();
    params.add("%SELLER%");
    
    if (keyword != null && !keyword.isEmpty()) {
      baseSql += "AND u.username LIKE ? ";
      params.add("%" + keyword + "%");
    }
    
    // 先获取基本用户列表
    String userSql = baseSql + "ORDER BY u.id LIMIT ? OFFSET ?";
    params.add(pageSize);
    params.add((pageNo - 1) * pageSize);
    
    List<Map<String, Object>> users = jdbc.query(userSql, (rs, rowNum) -> {
      Map<String, Object> row = new HashMap<>();
      row.put("id", rs.getLong("id"));
      row.put("username", rs.getString("username"));
      row.put("nickname", rs.getString("nickname"));
      row.put("status", rs.getString("status"));
      row.put("createdAt", rs.getTimestamp("createdAt") != null ? rs.getTimestamp("createdAt").toString() : null);
      return row;
    }, params.toArray());
    
    // 对每个用户统计商品数据
    for (Map<String, Object> user : users) {
      Long userId = (Long) user.get("id");
      
      int totalItems = countItemsBySellerId(userId);
      int activeItems = countItemsBySellerAndStatus(userId, "APPROVED");
      int pendingItems = countItemsBySellerAndStatus(userId, "PENDING_REVIEW");
      
      user.put("totalItems", totalItems);
      user.put("activeItems", activeItems);
      user.put("soldItems", 0); // 暂时返回0
      user.put("pendingItems", pendingItems);
    }
    
    return users;
  }

  /**
   * 统计卖家的商品总数
   */
  private int countItemsBySellerId(Long sellerId) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM item WHERE seller_id = ?", Integer.class, sellerId);
    return count != null ? count : 0;
  }

  /**
   * 统计卖家特定状态的商品数
   */
  private int countItemsBySellerAndStatus(Long sellerId, String status) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM item WHERE seller_id = ? AND review_status = ?", 
        Integer.class, sellerId, status);
    return count != null ? count : 0;
  }

  public int countVendors(String keyword) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT u.id) FROM user_account u WHERE u.roles LIKE ?");
    List<Object> params = new ArrayList<>();
    params.add("%SELLER%");
    if (keyword != null && !keyword.isEmpty()) {
      sql.append(" AND u.username LIKE ?");
      params.add("%" + keyword + "%");
    }
    Integer count = jdbc.queryForObject(sql.toString(), Integer.class, params.toArray());
    return count != null ? count : 0;
  }

  /**
   * 查询分类统计数据
   * @param sql SQL查询语句
   * @return 分类统计列表
   */
  public List<Map<String, Object>> queryForCategoryStats(String sql) {
    return jdbc.query(sql, (rs, rowNum) -> {
      Map<String, Object> row = new HashMap<>();
      row.put("name", rs.getString("category"));
      row.put("count", rs.getInt("count"));
      return row;
    });
  }

  /**
   * 获取最近发布的商品
   * @param limit 数量限制
   * @return 最近商品列表
   */
  public List<Map<String, Object>> findRecentItems(int limit) {
    String sql = "SELECT id, title, seller_id, price, created_at AS createdAt FROM item WHERE review_status = 'APPROVED' ORDER BY created_at DESC LIMIT ?";
    return jdbc.query(sql, (rs, rowNum) -> {
      Map<String, Object> row = new HashMap<>();
      row.put("id", rs.getLong("id"));
      row.put("title", rs.getString("title"));
      row.put("sellerId", rs.getLong("seller_id"));
      row.put("price", rs.getDouble("price"));
      row.put("createdAt", rs.getTimestamp("createdAt") != null ? rs.getTimestamp("createdAt").toString() : null);
      return row;
    }, limit);
  }
}

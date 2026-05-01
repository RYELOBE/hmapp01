package com.campus.marketplace.repository;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Repository
public class OrderRepository {

  private final JdbcTemplate jdbc;
  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("id", rs.getLong("id"));
    row.put("orderNo", rs.getString("order_no"));
    row.put("itemId", rs.getLong("item_id"));
    row.put("itemTitle", rs.getString("item_title"));
    row.put("itemImage", rs.getString("item_image"));
    row.put("buyerId", rs.getLong("buyer_id"));
    row.put("sellerId", rs.getLong("seller_id"));
    row.put("price", rs.getInt("price"));
    row.put("quantity", rs.getInt("quantity"));
    row.put("totalAmount", rs.getInt("total_amount"));
    row.put("status", rs.getString("status"));
    row.put("receiverName", rs.getString("receiver_name"));
    row.put("receiverPhone", rs.getString("receiver_phone"));
    row.put("receiverAddress", rs.getString("receiver_address"));
    row.put("expressCompany", rs.getString("express_company"));
    row.put("expressNo", rs.getString("express_no"));
    row.put("buyerName", rs.getString("buyer_name"));
    row.put("sellerName", rs.getString("seller_name"));
    row.put("createdAt", rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toString() : null);
    row.put("updatedAt", rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toString() : null);
    return row;
  };

  public OrderRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  /** 生成订单号 */
  private String generateOrderNo() {
    return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
           String.valueOf((int)(Math.random() * 1000));
  }

  public Map<String, Object> save(Long buyerId, Long sellerId, Long itemId,
      String itemTitle, String itemImage, Integer price, Integer quantity, 
      String receiverName, String receiverPhone, String receiverAddress, 
      String buyerName, String sellerName) {
    String orderNo = generateOrderNo();
    Integer totalAmount = price * quantity;
    
    KeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      var ps = con.prepareStatement(
          "INSERT INTO orders (order_no, item_id, item_title, item_image, buyer_id, seller_id, price, quantity, total_amount, status, receiver_name, receiver_phone, receiver_address, buyer_name, seller_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING_PAYMENT', ?, ?, ?, ?, ?)",
          Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, orderNo);
      ps.setLong(2, itemId);
      ps.setString(3, itemTitle);
      ps.setString(4, itemImage);
      ps.setLong(5, buyerId);
      ps.setLong(6, sellerId);
      ps.setInt(7, price);
      ps.setInt(8, quantity);
      ps.setInt(9, totalAmount);
      ps.setString(10, receiverName);
      ps.setString(11, receiverPhone);
      ps.setString(12, receiverAddress);
      ps.setString(13, buyerName);
      ps.setString(14, sellerName);
      return ps;
    }, kh);
    Long id = kh.getKey().longValue();
    return findById(id);
  }

  public Map<String, Object> findById(Long id) {
    List<Map<String, Object>> results = jdbc.query("SELECT * FROM orders WHERE id = ?", ROW_MAPPER, id);
    return results.isEmpty() ? null : results.get(0);
  }

  /** 检查是否有进行中的订单 */
  public boolean hasActiveOrder(Long buyerId, Long itemId) {
    List<String> activeStatuses = List.of("PENDING_PAYMENT", "PAID", "SHIPPED");
    String sql = "SELECT COUNT(*) FROM orders WHERE buyer_id = ? AND item_id = ? AND status IN (?, ?, ?)";
    Integer count = jdbc.queryForObject(sql, Integer.class, buyerId, itemId, "PENDING_PAYMENT", "PAID", "SHIPPED");
    return count != null && count > 0;
  }

  public List<Map<String, Object>> findAll() {
    return jdbc.query("SELECT * FROM orders ORDER BY created_at DESC, id DESC", ROW_MAPPER);
  }

  public List<Map<String, Object>> findByBuyerId(Long buyerId) {
    return jdbc.query("SELECT * FROM orders WHERE buyer_id = ? ORDER BY created_at DESC, id DESC", ROW_MAPPER, buyerId);
  }

  public List<Map<String, Object>> findBySellerId(Long sellerId) {
    return jdbc.query("SELECT * FROM orders WHERE seller_id = ? ORDER BY created_at DESC, id DESC", ROW_MAPPER, sellerId);
  }

  public void updateStatus(Long id, String status) {
    jdbc.update("UPDATE orders SET status = ? WHERE id = ?", status, id);
  }

  /** 发货 */
  public void ship(Long id, String expressCompany, String expressNo) {
    jdbc.update("UPDATE orders SET status = 'SHIPPED', express_company = ?, express_no = ? WHERE id = ?", expressCompany, expressNo, id);
  }

  /** 更新快递信息 */
  public void updateExpressInfo(Long id, String expressCompany, String expressNo) {
    jdbc.update("UPDATE orders SET express_company = ?, express_no = ? WHERE id = ?", expressCompany, expressNo, id);
  }

  // ── 运营端分页查询 ──────────────────────────
  public List<Map<String, Object>> findByPage(String status, String keyword, int pageNo, int pageSize) {
    StringBuilder sql = new StringBuilder("SELECT * FROM orders WHERE 1=1");
    if (status != null && !status.isEmpty()) sql.append(" AND status = ?");
    if (keyword != null && !keyword.isEmpty()) sql.append(" AND item_title LIKE ?");
    sql.append(" ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?");

    List<Object> params = new ArrayList<>();
    if (status != null && !status.isEmpty()) params.add(status);
    if (keyword != null && !keyword.isEmpty()) params.add("%" + keyword + "%");
    params.add(pageSize);
    params.add((pageNo - 1) * pageSize);

    return jdbc.query(sql.toString(), ROW_MAPPER, params.toArray());
  }

  public int countByFilter(String status, String keyword) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM orders WHERE 1=1");
    if (status != null && !status.isEmpty()) sql.append(" AND status = ?");
    if (keyword != null && !keyword.isEmpty()) sql.append(" AND item_title LIKE ?");

    List<Object> params = new ArrayList<>();
    if (status != null && !status.isEmpty()) params.add(status);
    if (keyword != null && !keyword.isEmpty()) params.add("%" + keyword + "%");

    Integer count = jdbc.queryForObject(sql.toString(), Integer.class, params.toArray());
    return count != null ? count : 0;
  }

  public int countAll() {
    Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);
    return count != null ? count : 0;
  }

  // ── 需方统计 ──────────────────────────
  public List<Map<String, Object>> findBuyersWithStats(String keyword, int pageNo, int pageSize) {
    StringBuilder sql = new StringBuilder(
        "SELECT u.id, u.username, u.nickname, " +
        "COUNT(o.id) AS totalOrders, " +
        "SUM(CASE WHEN o.status IN ('PENDING_PAYMENT','PAID') THEN 1 ELSE 0 END) AS paidOrders, " +
        "SUM(CASE WHEN o.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completedOrders, " +
        "COALESCE(SUM(o.total_amount), 0) AS totalSpent " +
        "FROM user_account u LEFT JOIN orders o ON u.id = o.buyer_id " +
        "WHERE u.roles LIKE ?");
    List<Object> params = new ArrayList<>();
    params.add("%BUYER%");

    if (keyword != null && !keyword.isEmpty()) {
      sql.append(" AND u.username LIKE ?");
      params.add("%" + keyword + "%");
    }
    sql.append(" GROUP BY u.id, u.username, u.nickname ORDER BY u.id LIMIT ? OFFSET ?");
    params.add(pageSize);
    params.add((pageNo - 1) * pageSize);
    return jdbc.query(sql.toString(), (rs, rowNum) -> {
      Map<String, Object> row = new HashMap<>();
      row.put("id", rs.getLong("id"));
      row.put("username", rs.getString("username"));
      row.put("nickname", rs.getString("nickname"));
      row.put("totalOrders", rs.getInt("totalOrders"));
      row.put("paidOrders", rs.getInt("paidOrders"));
      row.put("completedOrders", rs.getInt("completedOrders"));
      row.put("totalSpent", rs.getInt("totalSpent"));
      return row;
    });
  }

  public int countBuyers(String keyword) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT u.id) FROM user_account u WHERE u.roles LIKE ?");
    List<Object> params = new ArrayList<>();
    params.add("%BUYER%");
    if (keyword != null && !keyword.isEmpty()) {
      sql.append(" AND u.username LIKE ?");
      params.add("%" + keyword + "%");
    }
    Integer count = jdbc.queryForObject(sql.toString(), Integer.class, params.toArray());
    return count != null ? count : 0;
  }

  // ── 我的订单分页（按买家/卖家） ──────────────────────────
  public List<Map<String, Object>> findByBuyerIdPaged(Long buyerId, String status, int pageNo, int pageSize) {
    StringBuilder sql = new StringBuilder("SELECT * FROM orders WHERE buyer_id = ?");
    List<Object> params = new ArrayList<>();
    params.add(buyerId);
    if (status != null && !status.isEmpty()) {
      sql.append(" AND status = ?");
      params.add(status);
    }
    sql.append(" ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?");
    params.add(pageSize);
    params.add((pageNo - 1) * pageSize);
    return jdbc.query(sql.toString(), ROW_MAPPER, params.toArray());
  }

  public List<Map<String, Object>> findBySellerIdPaged(Long sellerId, String status, int pageNo, int pageSize) {
    StringBuilder sql = new StringBuilder("SELECT * FROM orders WHERE seller_id = ?");
    List<Object> params = new ArrayList<>();
    params.add(sellerId);
    if (status != null && !status.isEmpty()) {
      sql.append(" AND status = ?");
      params.add(status);
    }
    sql.append(" ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?");
    params.add(pageSize);
    params.add((pageNo - 1) * pageSize);
    return jdbc.query(sql.toString(), ROW_MAPPER, params.toArray());
  }

  public int countByBuyerIdWithFilter(Long buyerId, String status) {
    if (status != null && !status.isEmpty()) {
      Integer count = jdbc.queryForObject(
          "SELECT COUNT(*) FROM orders WHERE buyer_id = ? AND status = ?", Integer.class, buyerId, status);
      return count != null ? count : 0;
    }
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM orders WHERE buyer_id = ?", Integer.class, buyerId);
    return count != null ? count : 0;
  }

  public int countBySellerIdWithFilter(Long sellerId, String status) {
    if (status != null && !status.isEmpty()) {
      Integer count = jdbc.queryForObject(
          "SELECT COUNT(*) FROM orders WHERE seller_id = ? AND status = ?", Integer.class, sellerId, status);
      return count != null ? count : 0;
    }
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM orders WHERE seller_id = ?", Integer.class, sellerId);
    return count != null ? count : 0;
  }

  // ── 卖家统计方法 ──────────────────────────
  public int sumTotalAmountBySellerAndStatusAndTimeRange(Long sellerId, String status, LocalDateTime startTime, LocalDateTime endTime) {
    String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE seller_id = ? AND status = ? AND created_at >= ? AND created_at < ?";
    Integer sum = jdbc.queryForObject(sql, Integer.class, sellerId, status,
        java.sql.Timestamp.valueOf(startTime), java.sql.Timestamp.valueOf(endTime));
    return sum != null ? sum : 0;
  }

  public int countBySellerAndStatusAndTimeRange(Long sellerId, String status, LocalDateTime startTime, LocalDateTime endTime) {
    String sql = "SELECT COUNT(*) FROM orders WHERE seller_id = ? AND status = ? AND created_at >= ? AND created_at < ?";
    Integer count = jdbc.queryForObject(sql, Integer.class, sellerId, status,
        java.sql.Timestamp.valueOf(startTime), java.sql.Timestamp.valueOf(endTime));
    return count != null ? count : 0;
  }

  public int sumTotalAmountBySellerAndStatus(Long sellerId, String status) {
    String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE seller_id = ? AND status = ?";
    Integer sum = jdbc.queryForObject(sql, Integer.class, sellerId, status);
    return sum != null ? sum : 0;
  }

  public int countBySellerAndStatus(Long sellerId, String status) {
    String sql = "SELECT COUNT(*) FROM orders WHERE seller_id = ? AND status = ?";
    Integer count = jdbc.queryForObject(sql, Integer.class, sellerId, status);
    return count != null ? count : 0;
  }

  public int countByItemIdAndSellerIdAndStatus(Long itemId, Long sellerId, String status) {
    String sql = "SELECT COUNT(*) FROM orders WHERE item_id = ? AND seller_id = ? AND status = ?";
    Integer count = jdbc.queryForObject(sql, Integer.class, itemId, sellerId, status);
    return count != null ? count : 0;
  }

  /**
   * 按日期范围统计订单数量
   * @param status 订单状态（可选）
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @return 订单数量
   */
  public int countByDateRange(String status, LocalDateTime startTime, LocalDateTime endTime) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM orders WHERE created_at >= ? AND created_at < ?");
    List<Object> params = new ArrayList<>();
    params.add(java.sql.Timestamp.valueOf(startTime));
    params.add(java.sql.Timestamp.valueOf(endTime));

    if (status != null && !status.isEmpty()) {
      sql.append(" AND status = ?");
      params.add(status);
    }

    Integer count = jdbc.queryForObject(sql.toString(), Integer.class, params.toArray());
    return count != null ? count : 0;
  }

  /**
   * 统计今日新增订单数
   * @return 今日订单数量
   */
  public int countTodayOrders() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);
    return countByDateRange(null, startOfDay, endOfDay);
  }

  /**
   * 统计今日新增已完成订单数
   * @return 今日已完成订单数量
   */
  public int countTodayCompletedOrders() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);
    return countByDateRange("COMPLETED", startOfDay, endOfDay);
  }

  /**
   * 统计今日新增待支付订单数
   * @return 今日待支付订单数量
   */
  public int countTodayPendingPaymentOrders() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);
    return countByDateRange("PENDING_PAYMENT", startOfDay, endOfDay);
  }

  /**
   * 按日期范围统计订单总金额
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @return 订单总金额
   */
  public int sumTotalAmountByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
    String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE created_at >= ? AND created_at < ?";
    Integer sum = jdbc.queryForObject(sql, Integer.class,
        java.sql.Timestamp.valueOf(startTime), java.sql.Timestamp.valueOf(endTime));
    return sum != null ? sum : 0;
  }

  /**
   * 统计今日订单总金额
   * @return 今日订单总金额
   */
  public int sumTodayTotalAmount() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);
    return sumTotalAmountByDateRange(startOfDay, endOfDay);
  }

  /**
   * 按状态统计订单总金额
   * @param status 订单状态
   * @return 订单总金额
   */
  public int sumTotalAmountByStatus(String status) {
    String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status = ?";
    Integer sum = jdbc.queryForObject(sql, Integer.class, status);
    return sum != null ? sum : 0;
  }

  /**
   * 统计所有已完成订单的总金额
   * @return 已完成订单总金额
   */
  public int sumCompletedTotalAmount() {
    return sumTotalAmountByStatus("COMPLETED");
  }

  /**
   * 按日期范围和状态统计订单数量
   * @param status 订单状态
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @return 订单数量
   */
  public int countByStatusAndDateRange(String status, LocalDateTime startTime, LocalDateTime endTime) {
    String sql = "SELECT COUNT(*) FROM orders WHERE status = ? AND created_at >= ? AND created_at < ?";
    Integer count = jdbc.queryForObject(sql, Integer.class, status,
        java.sql.Timestamp.valueOf(startTime), java.sql.Timestamp.valueOf(endTime));
    return count != null ? count : 0;
  }

  /**
   * 按日期范围和状态统计订单金额
   * @param status 订单状态
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @return 订单总金额
   */
  public int sumTotalAmountByStatusAndDateRange(String status, LocalDateTime startTime, LocalDateTime endTime) {
    String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status = ? AND created_at >= ? AND created_at < ?";
    Integer sum = jdbc.queryForObject(sql, Integer.class, status,
        java.sql.Timestamp.valueOf(startTime), java.sql.Timestamp.valueOf(endTime));
    return sum != null ? sum : 0;
  }
}

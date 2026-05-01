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

@Repository
public class OrderRepository {

  private final JdbcTemplate jdbc;
  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("id", rs.getLong("id"));
    row.put("itemId", rs.getLong("item_id"));
    row.put("itemTitle", rs.getString("item_title"));
    row.put("buyerId", rs.getLong("buyer_id"));
    row.put("sellerId", rs.getLong("seller_id"));
    row.put("amount", rs.getInt("amount"));
    row.put("status", rs.getString("status"));
    row.put("itemPrice", rs.getObject("item_price") != null ? rs.getInt("item_price") : null);
    row.put("buyerName", rs.getString("buyer_name"));
    row.put("sellerName", rs.getString("seller_name"));
    row.put("createdAt", rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toString() : null);
    return row;
  };

  public OrderRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> save(Long buyerId, Long sellerId, Long itemId,
      String itemTitle, Integer amount, String buyerName, String sellerName) {
    KeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      var ps = con.prepareStatement(
          "INSERT INTO orders (item_id, item_title, buyer_id, seller_id, amount, status, item_price, buyer_name, seller_name) VALUES (?, ?, ?, ?, ?, 'CREATED', ?, ?, ?)",
          Statement.RETURN_GENERATED_KEYS);
      ps.setLong(1, itemId);
      ps.setString(2, itemTitle);
      ps.setLong(3, buyerId);
      ps.setLong(4, sellerId);
      ps.setInt(5, amount);
      ps.setInt(6, amount);
      ps.setString(7, buyerName);
      ps.setString(8, sellerName);
      return ps;
    }, kh);
    Long id = kh.getKey().longValue();
    List<Map<String, Object>> results = jdbc.query("SELECT * FROM orders WHERE id = ?", ROW_MAPPER, id);
    return results.isEmpty() ? null : results.get(0);
  }

  // 向后兼容
  public Map<String, Object> save(Long buyerId, Long sellerId, Long itemId,
      String itemTitle, Integer amount) {
    return save(buyerId, sellerId, itemId, itemTitle, amount, null, null);
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
        "SUM(CASE WHEN o.status IN ('CREATED','PAID') THEN 1 ELSE 0 END) AS paidOrders, " +
        "SUM(CASE WHEN o.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completedOrders, " +
        "COALESCE(SUM(o.amount), 0) AS totalSpent " +
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

  // ── 我的订单分页（按买家/卖家） ──────────────
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
}

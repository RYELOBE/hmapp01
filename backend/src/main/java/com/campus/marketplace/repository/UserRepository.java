package com.campus.marketplace.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

  private final JdbcTemplate jdbc;
  
  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("id", rs.getLong("id"));
    row.put("username", rs.getString("username"));
    row.put("password", rs.getString("password"));
    row.put("nickname", rs.getString("nickname"));
    row.put("roles", parseRoles(rs.getString("roles")));
    
    // 新增字段映射
    try { row.put("campus", rs.getString("campus")); } catch (Exception e) { row.put("campus", ""); }
    try { row.put("phone", rs.getString("phone")); } catch (Exception e) { row.put("phone", ""); }
    try { row.put("avatar", rs.getString("avatar")); } catch (Exception e) { row.put("avatar", ""); }
    try { row.put("email", rs.getString("email")); } catch (Exception e) { row.put("email", ""); }
    try { row.put("bio", rs.getString("bio")); } catch (Exception e) { row.put("bio", ""); }
    try { row.put("status", rs.getString("status")); } catch (Exception e) { row.put("status", "ACTIVE"); }
    try {
      Timestamp createdAt = rs.getTimestamp("created_at");
      row.put("createdAt", createdAt != null ? createdAt.toString() : null);
    } catch (Exception e) { row.put("createdAt", null); }
    
    return row;
  };

  public UserRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Optional<Map<String, Object>> findByUsername(String username) {
    List<Map<String, Object>> results = jdbc.query(
        "SELECT * FROM user_account WHERE username = ?", ROW_MAPPER, username);
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }

  public Optional<Map<String, Object>> findById(Long id) {
    List<Map<String, Object>> results = jdbc.query(
        "SELECT * FROM user_account WHERE id = ?", ROW_MAPPER, id);
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }

  public Map<String, Object> create(String username, String password, String nickname, List<String> roles) {
    jdbc.update(
        "INSERT INTO user_account (username, password, nickname, roles) VALUES (?, ?, ?, ?)",
        username, password, nickname, String.join(",", roles));
    return findByUsername(username).orElseThrow();
  }

  /** 将 roles 字段从 DB 的逗号分隔字符串转成 List */
  public static List<String> parseRoles(String rolesStr) {
    if (rolesStr == null || rolesStr.isBlank()) {
      return List.of();
    }
    return Arrays.asList(rolesStr.split(","));
  }

  /** 兼容旧代码中直接传 Object 的场景 */
  public static List<String> parseRoles(Object rolesObj) {
    if (rolesObj instanceof List<?> list) {
      return list.stream().map(Object::toString).toList();
    }
    if (rolesObj instanceof String s) {
      return parseRoles(s);
    }
    return List.of();
  }

  /**
   * 更新用户资料
   */
  public void updateProfile(Long userId, String nickname, String email, String bio, String campus, String phone) {
    StringBuilder sql = new StringBuilder("UPDATE user_account SET ");
    List<Object> params = new java.util.ArrayList<>();
    
    if (nickname != null) {
      sql.append("nickname=?, ");
      params.add(nickname);
    }
    if (email != null) {
      sql.append("email=?, ");
      params.add(email);
    }
    if (bio != null) {
      sql.append("bio=?, ");
      params.add(bio);
    }
    if (campus != null) {
      sql.append("campus=?, ");
      params.add(campus);
    }
    if (phone != null) {
      sql.append("phone=?, ");
      params.add(phone);
    }
    
    // 移除最后的逗号和空格
    sql.setLength(sql.length() - 2);
    sql.append(" WHERE id=?");
    params.add(userId);
    
    jdbc.update(sql.toString(), params.toArray());
  }

  /**
   * 更新用户头像
   */
  public void updateAvatar(Long userId, String avatarUrl) {
    jdbc.update("UPDATE user_account SET avatar = ? WHERE id = ?", avatarUrl, userId);
  }

  /**
   * 更新用户密码
   */
  public void updatePassword(Long userId, String newPassword) {
    jdbc.update("UPDATE user_account SET password = ? WHERE id = ?", newPassword, userId);
  }

  /**
   * 按日期范围查询用户数量
   */
  public int countByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
    String sql = "SELECT COUNT(*) FROM user_account WHERE created_at >= ? AND created_at < ?";
    Integer count = jdbc.queryForObject(sql, Integer.class,
        Timestamp.valueOf(startTime), Timestamp.valueOf(endTime));
    return count != null ? count : 0;
  }

  /**
   * 统计今日新增用户数
   */
  public int countTodayNewUsers() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);
    return countByDateRange(startOfDay, endOfDay);
  }

  /**
   * 统计活跃用户数（30天内有操作的用户）
   */
  public int countActiveUsers() {
    String sql = """
        SELECT COUNT(DISTINCT user_id) FROM (
          SELECT seller_id AS user_id FROM item WHERE updated_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
          UNION
          SELECT buyer_id AS user_id FROM orders WHERE updated_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
          UNION
          SELECT seller_id AS user_id FROM orders WHERE updated_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
        ) AS active_users
        """;
    Integer count = jdbc.queryForObject(sql, Integer.class);
    return count != null ? count : 0;
  }

  /**
   * 按角色统计用户数量
   */
  public int countByRole(String role) {
    String sql = "SELECT COUNT(*) FROM user_account WHERE roles LIKE ?";
    Integer count = jdbc.queryForObject(sql, Integer.class, "%" + role + "%");
    return count != null ? count : 0;
  }

  /**
   * 查询所有用户（分页）
   */
  public List<Map<String, Object>> findByPage(String keyword, int pageNo, int pageSize) {
    StringBuilder sql = new StringBuilder("SELECT * FROM user_account WHERE 1=1");
    List<Object> params = new java.util.ArrayList<>();

    if (keyword != null && !keyword.isEmpty()) {
      sql.append(" AND (username LIKE ? OR nickname LIKE ?)");
      params.add("%" + keyword + "%");
      params.add("%" + keyword + "%");
    }

    sql.append(" ORDER BY id DESC LIMIT ? OFFSET ?");
    params.add(pageSize);
    params.add((pageNo - 1) * pageSize);

    return jdbc.query(sql.toString(), ROW_MAPPER, params.toArray());
  }

  /**
   * 统计用户总数（带搜索条件）
   */
  public int countAll(String keyword) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM user_account WHERE 1=1");
    List<Object> params = new java.util.ArrayList<>();

    if (keyword != null && !keyword.isEmpty()) {
      sql.append(" AND (username LIKE ? OR nickname LIKE ?)");
      params.add("%" + keyword + "%");
      params.add("%" + keyword + "%");
    }

    Integer count = jdbc.queryForObject(sql.toString(), Integer.class, params.toArray());
    return count != null ? count : 0;
  }

  /**
   * 统计用户总数
   */
  public int countAll() {
    Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM user_account", Integer.class);
    return count != null ? count : 0;
  }

  /**
   * 更新用户角色
   */
  public void updateRoles(Long userId, List<String> roles) {
    jdbc.update("UPDATE user_account SET roles = ? WHERE id = ?", String.join(",", roles), userId);
  }

  /**
   * 更新用户状态
   */
  public void updateStatus(Long userId, String status) {
    jdbc.update("UPDATE user_account SET status = ? WHERE id = ?", status, userId);
  }
}

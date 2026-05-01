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
   * 按日期范围查询用户数量
   * @param startTime 开始时间（包含）
   * @param endTime 结束时间（不包含）
   * @return 用户数量
   */
  public int countByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
    String sql = "SELECT COUNT(*) FROM user_account WHERE created_at >= ? AND created_at < ?";
    Integer count = jdbc.queryForObject(sql, Integer.class,
        Timestamp.valueOf(startTime), Timestamp.valueOf(endTime));
    return count != null ? count : 0;
  }

  /**
   * 统计今日新增用户数
   * @return 今日新增用户数量
   */
  public int countTodayNewUsers() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);
    return countByDateRange(startOfDay, endOfDay);
  }

  /**
   * 统计活跃用户数（30天内有操作的用户）
   * 活跃定义：30天内在 item 表发布过商品或在 orders 表有订单的用户
   * @return 活跃用户数量
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
   * @param role 角色名称（如 SELLER, BUYER）
   * @return 用户数量
   */
  public int countByRole(String role) {
    String sql = "SELECT COUNT(*) FROM user_account WHERE roles LIKE ?";
    Integer count = jdbc.queryForObject(sql, Integer.class, "%" + role + "%");
    return count != null ? count : 0;
  }

  /**
   * 查询所有用户（分页）
   * @param keyword 搜索关键词（用户名或昵称）
   * @param pageNo 页码
   * @param pageSize 每页数量
   * @return 用户列表
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
   * @param keyword 搜索关键词
   * @return 用户数量
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
   * @return 用户数量
   */
  public int countAll() {
    Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM user_account", Integer.class);
    return count != null ? count : 0;
  }

  /**
   * 更新用户角色
   * @param userId 用户ID
   * @param roles 新角色列表
   */
  public void updateRoles(Long userId, List<String> roles) {
    jdbc.update("UPDATE user_account SET roles = ? WHERE id = ?", String.join(",", roles), userId);
  }

  /**
   * 更新用户状态
   * @param userId 用户ID
   * @param status 新状态
   */
  public void updateStatus(Long userId, String status) {
    jdbc.update("UPDATE user_account SET status = ? WHERE id = ?", status, userId);
  }
}

package com.campus.marketplace.repository;

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
}

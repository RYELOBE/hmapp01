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
public class AppRegisterRepository {

  private final JdbcTemplate jdbc;
  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("appCode", rs.getString("app_code"));
    row.put("title", rs.getString("title"));
    row.put("entry", rs.getString("entry"));
    row.put("pathPrefix", rs.getString("path_prefix"));
    try {
      row.put("portalCode", rs.getString("portal_code"));
    } catch (Exception e) {
      row.put("portalCode", "");
    }
    String rolesStr = rs.getString("roles");
    row.put("roles", (rolesStr == null || rolesStr.isBlank())
        ? List.<String>of()
        : Arrays.asList(rolesStr.split(",")));
    try {
      row.put("hideShellMenu", rs.getBoolean("hide_shell_menu"));
    } catch (Exception e) {
      row.put("hideShellMenu", false);
    }
    return row;
  };

  public AppRegisterRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> findAll() {
    return jdbc.query("SELECT * FROM app_register", ROW_MAPPER);
  }

  public Optional<Map<String, Object>> findByAppCode(String appCode) {
    List<Map<String, Object>> results = jdbc.query(
        "SELECT * FROM app_register WHERE app_code = ?", ROW_MAPPER, appCode);
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }

  public Map<String, Object> save(String appCode, String title, String entry,
      String pathPrefix, List<String> roles, boolean hideShellMenu) {
    String rolesStr = (roles == null || roles.isEmpty()) ? "" : String.join(",", roles);
    try {
      jdbc.update("""
          INSERT INTO app_register (app_code, title, entry, path_prefix, roles, hide_shell_menu, portal_code)
          VALUES (?, ?, ?, ?, ?, ?, ?)
          ON DUPLICATE KEY UPDATE title = VALUES(title), entry = VALUES(entry),
          path_prefix = VALUES(path_prefix), roles = VALUES(roles), hide_shell_menu = VALUES(hide_shell_menu), portal_code = VALUES(portal_code)
          """, appCode, title, entry, pathPrefix, rolesStr, hideShellMenu ? 1 : 0, "");
    } catch (Exception e) {
      // 如果 portal_code 列不存在，尝试不包含该列的 SQL
      jdbc.update("""
          INSERT INTO app_register (app_code, title, entry, path_prefix, roles, hide_shell_menu)
          VALUES (?, ?, ?, ?, ?, ?)
          ON DUPLICATE KEY UPDATE title = VALUES(title), entry = VALUES(entry),
          path_prefix = VALUES(path_prefix), roles = VALUES(roles), hide_shell_menu = VALUES(hide_shell_menu)
          """, appCode, title, entry, pathPrefix, rolesStr, hideShellMenu ? 1 : 0);
    }
    return findByAppCode(appCode).orElseThrow();
  }

  public boolean delete(String appCode) {
    int affected = jdbc.update("DELETE FROM app_register WHERE app_code = ?", appCode);
    return affected > 0;
  }
}

package com.campus.marketplace.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ResourceMenuRepository {

  private final JdbcTemplate jdbc;

  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("id", rs.getLong("id"));
    row.put("parentId", rs.getLong("parent_id"));
    row.put("menuCode", rs.getString("menu_code"));
    row.put("menuName", rs.getString("menu_name"));
    row.put("menuType", rs.getString("menu_type"));
    row.put("appCode", rs.getString("app_code"));
    row.put("path", rs.getString("path"));
    row.put("icon", rs.getString("icon"));
    row.put("sortOrder", rs.getInt("sort_order"));
    row.put("visible", rs.getBoolean("visible"));
    return row;
  };

  public ResourceMenuRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> findAll() {
    return jdbc.query("SELECT * FROM resource_menu ORDER BY sort_order, id", ROW_MAPPER);
  }

  public List<Map<String, Object>> findByAppCode(String appCode) {
    return jdbc.query(
        "SELECT * FROM resource_menu WHERE app_code = ? ORDER BY sort_order, id",
        ROW_MAPPER, appCode);
  }

  public List<Map<String, Object>> findByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    String placeholders = ids.stream().map(id -> "?").reduce((a, b) -> a + "," + b).orElse("");
    return jdbc.query(
        "SELECT * FROM resource_menu WHERE id IN (" + placeholders + ") ORDER BY sort_order, id",
        ROW_MAPPER, ids.toArray());
  }

  /** 查询某个角色绑定的资源菜单列表 */
  public List<Map<String, Object>> findByRoleCode(String roleCode) {
    return jdbc.query("""
        SELECT rm.* FROM resource_menu rm
        INNER JOIN role_resource rr ON rm.id = rr.resource_id
        WHERE rr.role_code = ?
        ORDER BY rm.sort_order, rm.id
        """, ROW_MAPPER, roleCode);
  }

  public Map<String, Object> save(String menuCode, String menuName, String menuType,
      String appCode, Long parentId, String path, String icon, int sortOrder) {
    jdbc.update("""
        INSERT INTO resource_menu (parent_id, menu_code, menu_name, menu_type, app_code, path, icon, sort_order)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), menu_type = VALUES(menu_type),
        app_code = VALUES(app_code), parent_id = VALUES(parent_id), path = VALUES(path),
        icon = VALUES(icon), sort_order = VALUES(sort_order)
        """, parentId, menuCode, menuName, menuType, appCode, path, icon, sortOrder);
    return findByMenuCode(menuCode).orElseThrow();
  }

  public Optional<Map<String, Object>> findByMenuCode(String menuCode) {
    List<Map<String, Object>> results = jdbc.query(
        "SELECT * FROM resource_menu WHERE menu_code = ?", ROW_MAPPER, menuCode);
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }

  public boolean delete(Long id) {
    int affected = jdbc.update("DELETE FROM resource_menu WHERE id = ?", id);
    return affected > 0;
  }
}

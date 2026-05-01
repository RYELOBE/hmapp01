package com.campus.marketplace.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class RoleResourceRepository {

  private final JdbcTemplate jdbc;

  private static final RowMapper<Long> ID_MAPPER = (rs, rowNum) -> rs.getLong("resource_id");

  public RoleResourceRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  /** 获取角色关联的所有资源 ID */
  public List<Long> findResourceIdsByRole(String roleCode) {
    return jdbc.query(
        "SELECT resource_id FROM role_resource WHERE role_code = ?", ID_MAPPER, roleCode);
  }

  /** 获取资源关联的所有角色 */
  public List<String> findRolesByResourceId(Long resourceId) {
    return jdbc.query(
        "SELECT role_code FROM role_resource WHERE resource_id = ?",
        (rs, rowNum) -> rs.getString("role_code"), resourceId);
  }

  /** 设置角色的资源列表（先删后增） */
  public void saveRoleResources(String roleCode, List<Long> resourceIds) {
    jdbc.update("DELETE FROM role_resource WHERE role_code = ?", roleCode);
    if (resourceIds != null && !resourceIds.isEmpty()) {
      jdbc.batchUpdate(
          "INSERT INTO role_resource (role_code, resource_id) VALUES (?, ?)",
          resourceIds.stream()
              .map(id -> new Object[]{roleCode, id})
              .toList());
    }
  }

  /** 获取所有角色列表（去重） */
  public List<String> findAllRoles() {
    return jdbc.query(
        "SELECT DISTINCT role_code FROM role_resource ORDER BY role_code",
        (rs, rowNum) -> rs.getString("role_code"));
  }

  private static final RowMapper<Map<String, Object>> ROLE_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("roleCode", rs.getString("role_code"));
    row.put("roleName", rs.getString("role_name"));
    row.put("description", rs.getString("description"));
    row.put("status", rs.getString("status"));
    return row;
  };

  public Map<String, Object> createRole(String roleCode, String roleName, String description) {
    jdbc.update("INSERT INTO ops_role (role_code, role_name, description, status) VALUES (?, ?, ?, 'ACTIVE')",
        roleCode, roleName, description);
    Map<String, Object> role = new HashMap<>();
    role.put("roleCode", roleCode);
    role.put("roleName", roleName);
    role.put("description", description);
    role.put("status", "ACTIVE");
    return role;
  }

  public void updateRole(String roleCode, String roleName, String description) {
    jdbc.update("UPDATE ops_role SET role_name = ?, description = ? WHERE role_code = ?",
        roleName, description, roleCode);
  }

  public void deleteRole(String roleCode) {
    jdbc.update("DELETE FROM role_resource WHERE role_code = ?", roleCode);
    jdbc.update("DELETE FROM ops_role WHERE role_code = ?", roleCode);
  }

  public void updateRoleStatus(String roleCode, String status) {
    jdbc.update("UPDATE ops_role SET status = ? WHERE role_code = ?", status, roleCode);
  }
}

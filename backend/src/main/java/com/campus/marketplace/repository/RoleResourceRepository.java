package com.campus.marketplace.repository;

import java.util.List;
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
}

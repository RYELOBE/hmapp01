package com.campus.marketplace.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class PortalConfigRepository {

  private final JdbcTemplate jdbc;

  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("id", rs.getLong("id"));
    row.put("portalCode", rs.getString("portal_code"));
    row.put("portalName", rs.getString("portal_name"));
    row.put("templateType", rs.getString("template_type"));
    row.put("configJson", rs.getString("config_json"));
    row.put("loginConfigId", rs.getObject("login_config_id"));
    row.put("updatedBy", rs.getString("updated_by"));
    row.put("updatedAt", rs.getTimestamp("updated_at"));
    return row;
  };

  public PortalConfigRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> findAll() {
    return jdbc.query("SELECT * FROM portal_config ORDER BY id", ROW_MAPPER);
  }

  public Optional<Map<String, Object>> findByPortalCode(String portalCode) {
    List<Map<String, Object>> results = jdbc.query(
        "SELECT * FROM portal_config WHERE portal_code = ?", ROW_MAPPER, portalCode);
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }

  public Map<String, Object> save(String portalCode, String portalName, String templateType,
      String configJson, String updatedBy) {
    jdbc.update("""
        INSERT INTO portal_config (portal_code, portal_name, template_type, config_json, updated_by)
        VALUES (?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE portal_name = VALUES(portal_name),
        template_type = VALUES(template_type), config_json = VALUES(config_json),
        updated_by = VALUES(updated_by)
        """, portalCode, portalName, templateType, configJson, updatedBy);
    return findByPortalCode(portalCode).orElseThrow();
  }

  public boolean delete(String portalCode) {
    int affected = jdbc.update("DELETE FROM portal_config WHERE portal_code = ?", portalCode);
    return affected > 0;
  }
}

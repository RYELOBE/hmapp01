package com.campus.marketplace.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class OpsAccountRepository {

  private final JdbcTemplate jdbc;
  private static final RowMapper<OpsAccount> ROW_MAPPER = (rs, rowNum) -> {
    OpsAccount account = new OpsAccount();
    account.setId(rs.getLong("id"));
    account.setUsername(rs.getString("username"));
    account.setPassword(rs.getString("password"));
    account.setNickname(rs.getString("nickname"));
    account.setRoles(rs.getString("roles"));
    account.setRoleLevel(rs.getString("role_level"));
    account.setStatus(rs.getString("status"));
    return account;
  };

  public OpsAccountRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Optional<OpsAccount> findByUsername(String username) {
    List<OpsAccount> results = jdbc.query(
        "SELECT * FROM ops_account WHERE username = ?", ROW_MAPPER, username);
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }

  public Optional<OpsAccount> findById(Long id) {
    List<OpsAccount> results = jdbc.query(
        "SELECT * FROM ops_account WHERE id = ?", ROW_MAPPER, id);
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }

  public OpsAccount create(String username, String password, String nickname, String roles, String roleLevel) {
    jdbc.update(
        "INSERT INTO ops_account (username, password, nickname, roles, role_level, status) VALUES (?, ?, ?, ?, ?, ?)",
        username, password, nickname, roles, roleLevel, "ACTIVE");
    return findByUsername(username).orElseThrow();
  }

  public static class OpsAccount {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String roles;
    private String roleLevel;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }
    public String getRoleLevel() { return roleLevel; }
    public void setRoleLevel(String roleLevel) { this.roleLevel = roleLevel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getRolesList() {
      if (roles == null || roles.isBlank()) {
        return List.of();
      }
      return List.of(roles.split(","));
    }
  }
}

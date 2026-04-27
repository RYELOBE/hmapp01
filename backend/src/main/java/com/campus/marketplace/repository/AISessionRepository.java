package com.campus.marketplace.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AISessionRepository {

  private final JdbcTemplate jdbc;
  private static final RowMapper<Map<String, Object>> ROW_MAPPER = (rs, rowNum) -> {
    Map<String, Object> row = new HashMap<>();
    row.put("sessionId", rs.getString("session_id"));
    row.put("userId", rs.getLong("user_id"));
    row.put("updatedAt", rs.getString("updated_at"));
    return row;
  };

  public AISessionRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  /** 不存在则新建，存在则复用，返回最终 sessionId */
  public String createOrReuseSession(String maybeSessionId, Long userId) {
    if (maybeSessionId != null) {
      Integer count = jdbc.queryForObject(
          "SELECT COUNT(1) FROM ai_session WHERE session_id = ?", Integer.class, maybeSessionId);
      if (count != null && count > 0) {
        return maybeSessionId;
      }
    }
    String sessionId = "S" + System.currentTimeMillis();
    jdbc.update(
        "INSERT INTO ai_session (session_id, user_id, updated_at) VALUES (?, ?, NOW())",
        sessionId, userId);
    return sessionId;
  }

  /** 更新会话最后操作时间 */
  public void updateSessionTime(String sessionId) {
    jdbc.update("UPDATE ai_session SET updated_at = NOW() WHERE session_id = ?", sessionId);
  }

  public List<Map<String, Object>> findRecentByUserId(Long userId) {
    return jdbc.query(
        "SELECT * FROM ai_session WHERE user_id = ? ORDER BY updated_at DESC LIMIT 10",
        ROW_MAPPER, userId);
  }
}

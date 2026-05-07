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
    row.put("id", rs.getLong("id"));
    row.put("sessionId", rs.getString("session_id"));
    row.put("userId", rs.getLong("user_id"));
    row.put("title", rs.getString("title"));
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
        "INSERT INTO ai_session (session_id, user_id, title, updated_at) VALUES (?, ?, '新对话', NOW())",
        sessionId, userId);
    return sessionId;
  }

  /** 更新会话标题（用第一条消息） */
  public void updateSessionTitle(String sessionId, String firstMessage) {
    String title = firstMessage.length() > 20 ? firstMessage.substring(0, 20) + "..." : firstMessage;
    jdbc.update("UPDATE ai_session SET title = ?, updated_at = NOW() WHERE session_id = ? AND (title IS NULL OR title = '新对话')",
        title, sessionId);
  }

  /** 更新会话最后操作时间 */
  public void updateSessionTime(String sessionId) {
    jdbc.update("UPDATE ai_session SET updated_at = NOW() WHERE session_id = ?", sessionId);
  }

  public List<Map<String, Object>> findRecentByUserId(Long userId) {
    List<Map<String, Object>> sessions = jdbc.query(
        "SELECT * FROM ai_session WHERE user_id = ? ORDER BY updated_at DESC LIMIT 10",
        ROW_MAPPER, userId);

    // 为没有标题的会话自动获取标题（取第一条用户消息）
    for (Map<String, Object> session : sessions) {
      String title = (String) session.get("title");
      if ("新对话".equals(title) || title == null || title.isEmpty()) {
        String sessionId = (String) session.get("sessionId");
        try {
          String firstMsg = jdbc.queryForObject(
              "SELECT content FROM ai_message WHERE session_id = ? AND role = 'user' ORDER BY id ASC LIMIT 1",
              String.class, sessionId);
          if (firstMsg != null) {
            String autoTitle = firstMsg.length() > 20 ? firstMsg.substring(0, 20) + "..." : firstMsg;
            session.put("title", autoTitle);
            jdbc.update("UPDATE ai_session SET title = ? WHERE session_id = ?", autoTitle, sessionId);
          }
        } catch (Exception e) {
          session.put("title", "历史对话");
        }
      }
    }
    return sessions;
  }
}

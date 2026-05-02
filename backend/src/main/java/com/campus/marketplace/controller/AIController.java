package com.campus.marketplace.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.campus.marketplace.service.AIService;
import com.campus.marketplace.service.CurrentUserService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@SaCheckLogin
public class AIController {
  private final AIService aiService;
  private final CurrentUserService currentUserService;

  public AIController(AIService aiService, CurrentUserService currentUserService) {
    this.aiService = aiService;
    this.currentUserService = currentUserService;
  }

  public record ChatRequest(String sessionId, String role, String message) {}

  /** AI 对话接口 */
  @PostMapping("/chat")
  public Map<String, Object> chat(@RequestBody ChatRequest req) {
    Long userId = currentUserService.userId();
    Map<String, Object> data = aiService.chat(userId, req.sessionId(), req.role(), req.message());
    return Map.of("code", 200, "data", data);
  }

  /** 获取预设问题列表 */
  @GetMapping("/presets")
  public Map<String, Object> presets() {
    List<String> roles = currentUserService.roles();
    String role = roles.isEmpty() ? "BUYER" : roles.get(0);
    return Map.of("code", 200, "data", Map.of("presets", aiService.getPresets(role)));
  }

  /** 获取最近的会话列表 */
  @GetMapping("/sessions/recent")
  public Map<String, Object> recentSessions() {
    Long userId = currentUserService.userId();
    return Map.of("code", 200, "data", Map.of("sessions", aiService.getRecentSessions(userId)));
  }

  /** 获取某个会话的消息记录 */
  @GetMapping("/sessions/{sessionId}/messages")
  public Map<String, Object> sessionMessages(@PathVariable("sessionId") String sessionId) {
    return Map.of("code", 200, "data", Map.of("messages", aiService.getMessages(sessionId)));
  }
}

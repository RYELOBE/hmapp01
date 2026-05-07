package com.campus.marketplace.controller;

import com.campus.marketplace.service.CurrentUserService;
import com.campus.marketplace.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

  private final MessageService messageService;
  private final CurrentUserService currentUserService;

  public MessageController(MessageService messageService, CurrentUserService currentUserService) {
    this.messageService = messageService;
    this.currentUserService = currentUserService;
  }

  @GetMapping
  public Map<String, Object> getMessages(
      @RequestParam(required = false) String type,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size) {
    Long userId = currentUserService.userId();
    List<Map<String, Object>> messages = messageService.getMessageList(userId, type, page, size);
    return buildSuccessResponse(Map.of("messages", messages));
  }

  @GetMapping("/unread-count")
  public Map<String, Object> getUnreadCount() {
    Long userId = currentUserService.userId();
    long unreadCount = messageService.getUnreadCount(userId);
    return buildSuccessResponse(Map.of("unreadCount", unreadCount));
  }

  @PostMapping("/{id}/read")
  public Map<String, Object> markAsRead(@PathVariable Long id) {
    Long userId = currentUserService.userId();
    messageService.markAsRead(id, userId);
    return buildSuccessResponse(Map.of("message", "标记成功"));
  }

  @PostMapping("/read-all")
  public Map<String, Object> markAllAsRead() {
    Long userId = currentUserService.userId();
    messageService.markAllAsRead(userId);
    return buildSuccessResponse(Map.of("message", "全部标记已读"));
  }

  @DeleteMapping("/{id}")
  public Map<String, Object> deleteMessage(@PathVariable Long id) {
    Long userId = currentUserService.userId();
    messageService.deleteMessage(id, userId);
    return buildSuccessResponse(Map.of("message", "删除成功"));
  }

  private Map<String, Object> buildSuccessResponse(Object data) {
    return Map.of("code", 200, "data", data);
  }
}

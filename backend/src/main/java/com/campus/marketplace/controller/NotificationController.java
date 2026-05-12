package com.campus.marketplace.controller;

import com.campus.marketplace.dto.NotificationDTO;
import com.campus.marketplace.dto.NotificationQueryDTO;
import com.campus.marketplace.service.CurrentUserService;
import com.campus.marketplace.service.NotificationService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationService notificationService;
  private final CurrentUserService currentUserService;

  public NotificationController(NotificationService notificationService, CurrentUserService currentUserService) {
    this.notificationService = notificationService;
    this.currentUserService = currentUserService;
  }

  private boolean isOps() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) return false;
    return auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_OPS"));
  }

  /**
   * 获取当前用户的未读消息数量
   */
  @GetMapping("/unread-count")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Map<String, Object>> getUnreadCount() {
    Long userId = currentUserService.userId();
    boolean ops = isOps();
    int count = notificationService.countUnreadForUser(userId, ops);
    return ResponseEntity.ok(Map.of("code", 200, "data", Map.of("count", count), "message", "success"));
  }

  /**
   * 获取当前用户的消息列表（支持分页和筛选）
   */
  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Map<String, Object>> getNotifications(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) Boolean unread) {
    Long userId = currentUserService.userId();
    boolean ops = isOps();

    NotificationQueryDTO query = new NotificationQueryDTO();
    query.setPage(page);
    query.setSize(size);
    query.setType(type);
    query.setUnread(unread);
    query.setReceiverId(userId);
    query.setIsOps(ops);

    List<NotificationDTO> notifications = notificationService.getNotifications(query);
    int total = notificationService.countNotifications(query);

    Map<String, Object> data = new HashMap<>();
    data.put("records", notifications);
    data.put("list", notifications);
    data.put("total", total);
    data.put("totalCount", total);
    data.put("pageNo", page);
    data.put("pageSize", size);

    return ResponseEntity.ok(buildSuccessResponse(data));
  }

  /**
   * 获取最近消息（用于首页展示）
   */
  @GetMapping("/recent")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Map<String, Object>> getRecentNotifications(
      @RequestParam(defaultValue = "5") int limit) {
    Long userId = currentUserService.userId();
    List<NotificationDTO> notifications = notificationService.getRecentNotifications(userId, limit);
    return ResponseEntity.ok(buildSuccessResponse(notifications));
  }

  /**
   * 获取未读消息列表
   */
  @GetMapping("/unread")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Map<String, Object>> getUnreadNotifications() {
    Long userId = currentUserService.userId();
    NotificationQueryDTO query = new NotificationQueryDTO();
    query.setReceiverId(userId);
    query.setUnread(true);
    List<NotificationDTO> notifications = notificationService.getNotifications(query);
    return ResponseEntity.ok(buildSuccessResponse(notifications));
  }

  /**
   * 获取消息详情
   */
  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Map<String, Object>> getNotification(@PathVariable Long id) {
    NotificationDTO notification = notificationService.getNotificationById(id);
    return ResponseEntity.ok(buildSuccessResponse(notification));
  }

  /**
   * 标记消息为已读
   */
  @PutMapping("/{id}/read")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long id) {
    notificationService.markAsRead(id);
    return ResponseEntity.ok(Map.of("code", 200, "message", "已标记为已读"));
  }

  /**
   * 批量标记为已读
   */
  @PostMapping("/mark-read")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Map<String, Object>> markMultipleAsRead(@RequestBody List<Long> ids) {
    notificationService.markMultipleAsRead(ids);
    return ResponseEntity.ok(Map.of("code", 200, "message", "已标记为已读"));
  }

  /**
   * 标记所有消息为已读
   */
  @PostMapping("/mark-all-read")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Map<String, Object>> markAllAsRead() {
    Long userId = currentUserService.userId();
    notificationService.markAllAsRead(userId);
    return ResponseEntity.ok(Map.of("code", 200, "message", "已全部标记为已读"));
  }

  /**
   * 删除消息
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long id) {
    notificationService.deleteNotification(id);
    return ResponseEntity.ok(Map.of("code", 200, "message", "已删除"));
  }

  /**
   * 发送系统通知（运营人员使用）
   */
  @PostMapping("/send")
  @PreAuthorize("hasRole('OPS')")
  public ResponseEntity<Map<String, Object>> sendNotification(@RequestBody NotificationSendRequest request) {
    notificationService.sendNotification(
        request.receiverId(),
        request.title(),
        request.content(),
        request.type(),
        request.businessId(),
        request.businessType()
    );
    return ResponseEntity.ok(Map.of("code", 200, "message", "通知已发送"));
  }

  private Map<String, Object> buildSuccessResponse(Object data) {
    Map<String, Object> response = new HashMap<>();
    response.put("code", 200);
    response.put("data", data);
    response.put("message", "success");
    return response;
  }

  public record NotificationSendRequest(
      Long receiverId,
      String title,
      String content,
      String type,
      String businessId,
      String businessType
  ) {}
}

package com.campus.marketplace.controller;

import com.campus.marketplace.dto.NotificationDTO;
import com.campus.marketplace.dto.NotificationQueryDTO;
import com.campus.marketplace.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public Map<String, Object> getNotifications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean unread) {
        NotificationQueryDTO query = new NotificationQueryDTO();
        query.setPage(page);
        query.setSize(size);
        query.setType(type);
        query.setUnread(unread);
        
        List<NotificationDTO> notifications = notificationService.getNotifications(query);
        long total = notificationService.countNotifications(query.getType(), query.getUnread(), query.getPriority(), query.getKeyword());
        return Map.of("code", 200, "data", notifications, "total", total);
    }

    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount() {
        Integer count = notificationService.getUnreadCount();
        return Map.of("code", 200, "data", count);
    }

    @GetMapping("/recent")
    public Map<String, Object> getRecentNotifications(
            @RequestParam(defaultValue = "5") Integer limit) {
        List<NotificationDTO> notifications = notificationService.getRecentNotifications(limit);
        return Map.of("code", 200, "data", notifications);
    }

    @PostMapping("/{id}/read")
    public Map<String, Object> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Map.of("code", 200, "message", "已标记为已读");
    }

    @PostMapping("/mark-read")
    public Map<String, Object> markMultipleAsRead(@RequestBody List<Long> ids) {
        notificationService.markMultipleAsRead(ids);
        return Map.of("code", 200, "message", "已批量标记为已读");
    }

    @PostMapping("/mark-all-read")
    public Map<String, Object> markAllAsRead() {
        notificationService.markAllAsRead();
        return Map.of("code", 200, "message", "已标记全部为已读");
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return Map.of("code", 200, "message", "消息已删除");
    }

    @PostMapping("/send")
    public Map<String, Object> sendNotification(@RequestBody NotificationDTO notification) {
        notificationService.sendNotification(notification);
        return Map.of("code", 200, "message", "通知已发送");
    }
}

package com.campus.marketplace.service.impl;

import com.campus.marketplace.dto.NotificationDTO;
import com.campus.marketplace.dto.NotificationQueryDTO;
import com.campus.marketplace.repository.NotificationRepository;
import com.campus.marketplace.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<NotificationDTO> getNotifications(NotificationQueryDTO query) {
        int page = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int size = query.getSize() == null || query.getSize() < 1 ? 20 : query.getSize();
        Long receiverId = query.getReceiverId();
        boolean isOps = Boolean.TRUE.equals(query.getIsOps());

        List<Map<String, Object>> results;
        if (receiverId != null) {
            if (Boolean.TRUE.equals(query.getUnread())) {
                results = notificationRepository.findUnreadByReceiverId(receiverId);
            } else {
                results = notificationRepository.findByReceiverIdOrOpsPaged(receiverId, isOps, page, size);
            }
        } else {
            results = notificationRepository.findAllPaged(page, size);
        }

        // 按类型筛选
        if (query.getType() != null && !query.getType().isEmpty()) {
            results = results.stream()
                .filter(r -> query.getType().equals(r.get("type")))
                .collect(Collectors.toList());
        }

        return results.stream()
                .map(this::convertMapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationDTO getNotificationById(Long id) {
        Map<String, Object> result = notificationRepository.findById(id);
        if (result == null) {
            throw new IllegalArgumentException("通知不存在");
        }
        return convertMapToDTO(result);
    }

    @Override
    public int countUnread(Long userId) {
        return notificationRepository.countUnreadByReceiverId(userId);
    }

    @Override
    public int countUnreadForUser(Long userId, boolean isOps) {
        return notificationRepository.countUnreadByReceiverIdOrOps(userId, isOps);
    }

    @Override
    public int countNotifications(NotificationQueryDTO query) {
        Long receiverId = query.getReceiverId();
        boolean isOps = Boolean.TRUE.equals(query.getIsOps());
        if (receiverId != null) {
            if (Boolean.TRUE.equals(query.getUnread())) {
                return notificationRepository.countUnreadByReceiverIdOrOps(receiverId, isOps);
            }
            return notificationRepository.countByReceiverIdOrOps(receiverId, isOps);
        }
        return notificationRepository.countAll();
    }

    @Override
    public List<NotificationDTO> getRecentNotifications(Long userId, int limit) {
        int realLimit = limit < 1 ? 5 : limit;
        List<Map<String, Object>> results;
        if (userId != null) {
            results = notificationRepository.findByReceiverId(userId, realLimit);
        } else {
            results = notificationRepository.findAll(realLimit);
        }
        return results.stream()
                .map(this::convertMapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(Long id) {
        notificationRepository.markAsRead(id);
    }

    @Override
    public void markMultipleAsRead(List<Long> ids) {
        if (ids != null) {
            for (Long id : ids) {
                notificationRepository.markAsRead(id);
            }
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Override
    public void deleteNotification(Long id) {
        notificationRepository.softDelete(id);
    }

    @Override
    public void sendNotification(Long receiverId, String title, String content, String type, String businessId, String businessType) {
        String priority = "MEDIUM";
        notificationRepository.save(title, content, type, receiverId, null, "系统", businessId, businessType, priority);
    }

    @Override
    public void createBusinessNotification(String businessType, String businessId, String title, String content, Long receiverId) {
        notificationRepository.save(title, content, businessType, receiverId, null, null, businessId, businessType, "MEDIUM");
    }

    private NotificationDTO convertMapToDTO(Map<String, Object> map) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId((Long) map.get("id"));
        dto.setTitle((String) map.get("title"));
        dto.setContent((String) map.get("content"));
        dto.setType((String) map.get("type"));
        dto.setIsRead((Boolean) map.get("isRead"));
        dto.setReceiverId((Long) map.get("receiverId"));
        dto.setSenderId((Long) map.get("senderId"));
        dto.setSenderName((String) map.get("senderName"));
        dto.setBusinessId((String) map.get("businessId"));
        dto.setBusinessType((String) map.get("businessType"));
        dto.setPriority((String) map.get("priority"));
        dto.setStatus((String) map.get("status"));
        dto.setCreatedAt(parseDateTime((String) map.get("createdAt")));
        dto.setReadAt(parseDateTime((String) map.get("readAt")));
        return dto;
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateStr.replace(" ", "T"));
        } catch (Exception e) {
            return null;
        }
    }
}

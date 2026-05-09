package com.campus.marketplace.service.impl;

import com.campus.marketplace.dto.NotificationDTO;
import com.campus.marketplace.dto.NotificationQueryDTO;
import com.campus.marketplace.entity.Notification;
import com.campus.marketplace.repository.NotificationRepository;
import com.campus.marketplace.service.NotificationService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
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

        List<Notification> notifications = notificationRepository.query(
            query.getType(),
            query.getUnread(),
            query.getPriority(),
            query.getKeyword(),
            page,
            size
        );
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Integer getUnreadCount() {
        return Math.toIntExact(notificationRepository.countUnread());
    }

    @Override
    public List<NotificationDTO> getRecentNotifications(Integer limit) {
        int realLimit = limit == null || limit < 1 ? 5 : limit;
        List<Notification> notifications = notificationRepository.findRecent(realLimit);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(Long id) {
        notificationRepository.markRead(id);
    }

    @Override
    public void markMultipleAsRead(List<Long> ids) {
        notificationRepository.markReadBatch(ids);
    }

    @Override
    public void markAllAsRead() {
        notificationRepository.markAllRead();
    }

    @Override
    public void deleteNotification(Long id) {
        notificationRepository.softDelete(id);
    }

    @Override
    public void sendNotification(NotificationDTO notificationDTO) {
        Notification notification = convertToEntity(notificationDTO);
        notification.setIsRead(notification.getIsRead() != null && notification.getIsRead());
        if (notification.getStatus() == null) {
            notification.setStatus("ACTIVE");
        }
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        notificationRepository.insert(notification);
    }

    @Override
    public void createBusinessNotification(String businessType, String businessId, String title, String content, Long receiverId) {
        notificationRepository.createBusinessNotification(businessType, businessId, title, content, receiverId);
    }

    @Override
    public long countNotifications(String type, Boolean unread, String priority, String keyword) {
        return notificationRepository.count(type, unread, priority, keyword);
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        BeanUtils.copyProperties(notification, dto);
        return dto;
    }

    private Notification convertToEntity(NotificationDTO dto) {
        Notification entity = new Notification();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}

package com.campus.marketplace.service;

import com.campus.marketplace.dto.NotificationDTO;
import com.campus.marketplace.dto.NotificationQueryDTO;

import java.util.List;

public interface NotificationService {
    
    /**
     * 分页获取消息列表
     */
    List<NotificationDTO> getNotifications(NotificationQueryDTO query);
    
    /**
     * 获取未读消息数量
     */
    Integer getUnreadCount();
    
    /**
     * 获取最近消息
     */
    List<NotificationDTO> getRecentNotifications(Integer limit);
    
    /**
     * 标记消息为已读
     */
    void markAsRead(Long id);
    
    /**
     * 批量标记为已读
     */
    void markMultipleAsRead(List<Long> ids);
    
    /**
     * 标记全部为已读
     */
    void markAllAsRead();
    
    /**
     * 删除消息
     */
    void deleteNotification(Long id);
    
    /**
     * 发送通知
     */
    void sendNotification(NotificationDTO notification);
    
    /**
     * 自动生成业务通知
     */
    void createBusinessNotification(String businessType, String businessId, String title, String content, Long receiverId);

    long countNotifications(String type, Boolean unread, String priority, String keyword);
}

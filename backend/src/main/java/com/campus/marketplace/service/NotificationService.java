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
     * 获取消息详情
     */
    NotificationDTO getNotificationById(Long id);
    
    /**
     * 获取用户未读消息数量
     */
    int countUnread(Long userId);
    
    /**
     * 统计消息数量
     */
    int countNotifications(NotificationQueryDTO query);
    
    /**
     * 获取用户最近消息
     */
    List<NotificationDTO> getRecentNotifications(Long userId, int limit);
    
    /**
     * 标记消息为已读
     */
    void markAsRead(Long id);
    
    /**
     * 批量标记为已读
     */
    void markMultipleAsRead(List<Long> ids);
    
    /**
     * 标记用户全部消息为已读
     */
    void markAllAsRead(Long userId);
    
    /**
     * 删除消息（软删除）
     */
    void deleteNotification(Long id);
    
    /**
     * 发送通知给指定用户
     */
    void sendNotification(Long receiverId, String title, String content, String type, String businessId, String businessType);
    
    /**
     * 自动生成业务通知
     */
    void createBusinessNotification(String businessType, String businessId, String title, String content, Long receiverId);
}

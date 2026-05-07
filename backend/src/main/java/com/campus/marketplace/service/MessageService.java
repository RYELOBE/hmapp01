package com.campus.marketplace.service;

import com.campus.marketplace.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MessageService {

  private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

  private final MessageRepository messageRepository;

  public MessageService(MessageRepository messageRepository) {
    this.messageRepository = messageRepository;
  }

  public Map<String, Object> sendMessage(Long userId, String type, String title, String content, String link) {
    logger.info("发送消息给用户 {}: [{}] {}", userId, type, title);
    return messageRepository.save(userId, type, title, content, link);
  }

  public List<Map<String, Object>> getMessageList(Long userId, String type, int page, int size) {
    if (type != null && !type.isEmpty()) {
      return messageRepository.findByUserIdAndTypeAndIsDeletedFalse(userId, type, page, size);
    }
    return messageRepository.findByUserIdAndIsDeletedFalseOrderByCreateTimeDesc(userId, page, size);
  }

  public long getUnreadCount(Long userId) {
    return messageRepository.countByUserIdAndStatusAndIsDeletedFalse(userId, "UNREAD");
  }

  public void markAsRead(Long messageId, Long userId) {
    Map<String, Object> msg = messageRepository.findById(messageId);
    if (msg == null) {
      throw new RuntimeException("消息不存在");
    }
    Long msgUserId = ((Number) msg.get("userId")).longValue();
    if (!msgUserId.equals(userId)) {
      throw new RuntimeException("无权操作");
    }
    messageRepository.updateStatus(messageId, "READ");
    logger.info("标记消息 {} 为已读", messageId);
  }

  public void markAllAsRead(Long userId) {
    messageRepository.markAllAsReadByUserId(userId);
    logger.info("标记用户 {} 的所有消息为已读", userId);
  }

  public void sendSystemNotification(Long userId, String title, String content, String link) {
    sendMessage(userId, "SYSTEM", title, content, link);
  }

  public void sendTransactionMessage(Long userId, String title, String content, String link) {
    sendMessage(userId, "TRANSACTION", title, content, link);
  }

  public void sendReviewResultMessage(Long userId, String title, String content, String link) {
    sendMessage(userId, "REVIEW", title, content, link);
  }

  public void sendInteractionMessage(Long userId, String title, String content, String link) {
    sendMessage(userId, "INTERACTION", title, content, link);
  }

  public void deleteMessage(Long messageId, Long userId) {
    Map<String, Object> msg = messageRepository.findById(messageId);
    if (msg == null) {
      throw new RuntimeException("消息不存在");
    }
    Long msgUserId = ((Number) msg.get("userId")).longValue();
    if (!msgUserId.equals(userId)) {
      throw new RuntimeException("无权操作");
    }
    messageRepository.softDelete(messageId);
    logger.info("删除消息: {}", messageId);
  }
}

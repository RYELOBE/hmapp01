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

  public Map<String, Object> sendMessage(Long senderId, String senderName, Long receiverId, String content, String type) {
    logger.info("发送消息从 {} 给用户 {}: [{}] {}", senderId, receiverId, type, content);
    return messageRepository.save(senderId, senderName, receiverId, content, type);
  }

  public List<Map<String, Object>> getMessageList(Long receiverId, String type, int page, int size) {
    if (type != null && !type.isEmpty()) {
      return messageRepository.findByReceiverIdAndType(receiverId, type, page, size);
    }
    return messageRepository.findByReceiverId(receiverId, page, size);
  }

  public long getUnreadCount(Long receiverId) {
    return messageRepository.countByReceiverIdAndStatus(receiverId, "UNREAD");
  }

  public void markAsRead(Long messageId, Long userId) {
    Map<String, Object> msg = messageRepository.findById(messageId);
    if (msg == null) {
      throw new RuntimeException("消息不存在");
    }
    Long msgReceiverId = ((Number) msg.get("receiverId")).longValue();
    if (!msgReceiverId.equals(userId)) {
      throw new RuntimeException("无权操作");
    }
    messageRepository.markAsRead(messageId);
    logger.info("标记消息 {} 为已读", messageId);
  }

  public void markAllAsRead(Long receiverId) {
    messageRepository.markAllAsReadByReceiverId(receiverId);
    logger.info("标记用户 {} 的所有消息为已读", receiverId);
  }

  public void sendSystemNotification(Long receiverId, String content) {
    sendMessage(0L, "系统", receiverId, content, "SYSTEM");
  }

  public void sendTransactionMessage(Long receiverId, String content) {
    sendMessage(0L, "系统", receiverId, content, "TRANSACTION");
  }

  public void sendReviewResultMessage(Long receiverId, String content) {
    sendMessage(0L, "系统", receiverId, content, "REVIEW");
  }

  public void sendInteractionMessage(Long receiverId, String content) {
    sendMessage(0L, "系统", receiverId, content, "INTERACTION");
  }

  public void deleteMessage(Long messageId, Long userId) {
    Map<String, Object> msg = messageRepository.findById(messageId);
    if (msg == null) {
      throw new RuntimeException("消息不存在");
    }
    Long msgReceiverId = ((Number) msg.get("receiverId")).longValue();
    if (!msgReceiverId.equals(userId)) {
      throw new RuntimeException("无权操作");
    }
    messageRepository.deleteById(messageId);
    logger.info("删除消息: {}", messageId);
  }
}

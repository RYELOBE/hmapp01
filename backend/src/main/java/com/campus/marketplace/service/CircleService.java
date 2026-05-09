package com.campus.marketplace.service;

import com.campus.marketplace.repository.CirclePostRepository;
import com.campus.marketplace.repository.CircleCommentRepository;
import com.campus.marketplace.repository.CircleLikeRepository;
import com.campus.marketplace.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CircleService {

  private static final Logger logger = LoggerFactory.getLogger(CircleService.class);

  private final CirclePostRepository postRepository;
  private final CircleCommentRepository commentRepository;
  private final CircleLikeRepository likeRepository;
  private final UserRepository userRepository;

  public CircleService(CirclePostRepository postRepository, CircleCommentRepository commentRepository,
      CircleLikeRepository likeRepository, UserRepository userRepository) {
    this.postRepository = postRepository;
    this.commentRepository = commentRepository;
    this.likeRepository = likeRepository;
    this.userRepository = userRepository;
  }

  public Map<String, Object> createPost(Long userId, String title, String content, String images, String tags) {
    logger.info("用户 {} 发布新帖子: {}", userId, title);
    String userName = userRepository.findById(userId)
        .map(u -> (String) u.get("nickname"))
        .orElse("");
    return postRepository.save(userId, userName, title, content, images, tags);
  }

  public List<Map<String, Object>> getPostList(int page, int size, String tag) {
    if (tag != null && !tag.isEmpty()) {
      return postRepository.findByTagAndStatus(tag, "APPROVED", page, size);
    }
    return postRepository.findByStatus("APPROVED", page, size);
  }

  public Map<String, Object> getPostDetail(Long postId) {
    Map<String, Object> post = postRepository.findById(postId);
    if (post == null) {
      throw new RuntimeException("帖子不存在");
    }
    postRepository.incrementViewCount(postId);
    post.put("viewCount", ((Number) post.get("viewCount")).intValue() + 1);
    logger.info("查看帖子详情: {}", postId);
    return post;
  }

  public void deletePost(Long postId, Long userId) {
    Map<String, Object> post = postRepository.findById(postId);
    if (post == null) {
      throw new RuntimeException("帖子不存在");
    }
    Long postUserId = ((Number) post.get("userId")).longValue();
    if (!postUserId.equals(userId)) {
      throw new RuntimeException("无权删除此帖子");
    }
    postRepository.delete(postId);
    logger.info("用户 {} 删除帖子: {}", userId, postId);
  }

  public boolean toggleLike(Long postId, Long userId) {
    var existingLike = likeRepository.findByPostIdAndUserId(postId, userId);

    if (existingLike.isPresent()) {
      likeRepository.deleteByPostIdAndUserId(postId, userId);
      postRepository.decrementLikeCount(postId);
      logger.info("用户 {} 取消点赞帖子: {}", userId, postId);
      return false;
    } else {
      likeRepository.save(postId, userId);
      postRepository.incrementLikeCount(postId);
      logger.info("用户 {} 点赞帖子: {}", userId, postId);
      return true;
    }
  }

  public Map<String, Object> addComment(Long postId, Long userId, String content) {
    Map<String, Object> post = postRepository.findById(postId);
    if (post == null) {
      throw new RuntimeException("帖子不存在");
    }
    String userName = userRepository.findById(userId)
        .map(u -> (String) u.get("nickname"))
        .orElse("");
    Map<String, Object> comment = commentRepository.save(postId, userId, userName, content);
    postRepository.incrementCommentCount(postId);
    logger.info("用户 {} 在帖子 {} 发表评论: {}", userId, postId, content);
    return comment;
  }

  public List<Map<String, Object>> getComments(Long postId, int page, int size) {
    return commentRepository.findByPostIdOrderByCreateTimeAsc(postId, page, size);
  }

  public List<Map<String, Object>> getPendingPosts(int page, int size) {
    return postRepository.findByStatus("PENDING", page, size);
  }

  public Map<String, Object> approvePost(Long postId) {
    Map<String, Object> post = postRepository.findById(postId);
    if (post == null) {
      throw new RuntimeException("帖子不存在");
    }
    postRepository.updateStatus(postId, "APPROVED");
    logger.info("审核通过帖子: {}", postId);
    return postRepository.findById(postId);
  }

  public Map<String, Object> rejectPost(Long postId, String reason) {
    Map<String, Object> post = postRepository.findById(postId);
    if (post == null) {
      throw new RuntimeException("帖子不存在");
    }
    postRepository.updateStatus(postId, "REJECTED");
    logger.info("审核拒绝帖子: {}, 原因: {}", postId, reason);
    return postRepository.findById(postId);
  }

  public long getPendingCount() {
    return postRepository.countByStatus("PENDING");
  }

  public List<Map<String, Object>> getUserPosts(Long userId, int page, int size) {
    return postRepository.findByUserIdAndStatus(userId, "APPROVED", page, size);
  }

  public List<Map<String, Object>> getPendingComments(int page, int size) {
    return commentRepository.findByStatusPaged("PENDING", page, size);
  }

  public long getPendingCommentCount() {
    return commentRepository.countByStatus("PENDING");
  }

  public List<Map<String, Object>> getReviewedComments(int page, int size) {
    return commentRepository.findByStatusNotPaged("PENDING", page, size);
  }

  public long getReviewedCommentCount() {
    return commentRepository.countByStatusNot("PENDING");
  }

  public void approveComment(Long commentId) {
    Map<String, Object> comment = commentRepository.findById(commentId);
    if (comment == null) {
      throw new RuntimeException("评论不存在");
    }
    commentRepository.updateStatus(commentId, "APPROVED");
    logger.info("审核通过评论: {}", commentId);
  }

  public void rejectComment(Long commentId) {
    Map<String, Object> comment = commentRepository.findById(commentId);
    if (comment == null) {
      throw new RuntimeException("评论不存在");
    }
    commentRepository.updateStatus(commentId, "REJECTED");
    logger.info("审核拒绝评论: {}", commentId);
  }
}

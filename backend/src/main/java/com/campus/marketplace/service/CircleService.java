package com.campus.marketplace.service;

import com.campus.marketplace.repository.CircleCommentLikeRepository;
import com.campus.marketplace.repository.CircleCommentRepository;
import com.campus.marketplace.repository.CircleLikeRepository;
import com.campus.marketplace.repository.CirclePostRepository;
import com.campus.marketplace.repository.UserRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CircleService {

  private final CirclePostRepository postRepository;
  private final CircleCommentRepository commentRepository;
  private final CircleLikeRepository likeRepository;
  private final CircleCommentLikeRepository commentLikeRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;
  private final Logger logger = LoggerFactory.getLogger(CircleService.class);

  public CircleService(CirclePostRepository postRepository, CircleCommentRepository commentRepository,
      CircleLikeRepository likeRepository, CircleCommentLikeRepository commentLikeRepository,
      UserRepository userRepository, NotificationService notificationService) {
    this.postRepository = postRepository;
    this.commentRepository = commentRepository;
    this.likeRepository = likeRepository;
    this.commentLikeRepository = commentLikeRepository;
    this.userRepository = userRepository;
    this.notificationService = notificationService;
  }

  public Map<String, Object> createPost(Long userId, String title, String content, String images, String tags) {
    logger.info("用户 {} 发布新帖子: {}", userId, title);
    String userName = userRepository.findById(userId)
        .map(u -> (String) u.get("nickname"))
        .orElse("");
    Map<String, Object> savedPost = postRepository.save(userId, userName, title, content, images, tags);

    // 发送发布成功通知给发帖人
    try {
      Long postId = ((Number) savedPost.get("id")).longValue();
      notificationService.sendNotification(
          userId,
          "圈子发布成功",
          String.format("您的帖子《%s》已提交审核，审核通过后将展示在圈子中", title),
          "CIRCLE",
          String.valueOf(postId),
          "CIRCLE"
      );
      logger.info("✅ 已发送圈子发布通知给用户 {}", userId);
    } catch (Exception e) {
      logger.warn("发送圈子发布通知失败: {}", e.getMessage());
    }

    return savedPost;
  }

  public List<Map<String, Object>> getPostList(int page, int size, String tag) {
    if (tag != null && !tag.isEmpty()) {
      return postRepository.findByTagAndStatus(tag, "APPROVED", page, size);
    }
    return postRepository.findByStatus("APPROVED", page, size);
  }

  public List<Map<String, Object>> getPostsByUserId(Long userId, int page, int size) {
    return postRepository.findByUserId(userId, page, size);
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
      
      // 发送点赞通知给帖子作者
      try {
        Map<String, Object> post = postRepository.findById(postId);
        if (post != null) {
          Long postAuthorId = ((Number) post.get("userId")).longValue();
          if (!postAuthorId.equals(userId)) {
            String postTitle = (String) post.get("title");
            String userName = userRepository.findById(userId)
                .map(u -> (String) u.get("nickname"))
                .orElse("用户");
            notificationService.sendNotification(
                postAuthorId, "收到点赞",
                String.format("%s 赞了你的帖子《%s》", userName, postTitle),
                "CIRCLE", String.valueOf(postId), "CIRCLE");
          }
        }
      } catch (Exception e) {
        logger.warn("发送点赞通知失败: {}", e.getMessage());
      }
      
      return true;
    }
  }

  public Map<String, Object> addComment(Long postId, Long userId, String content) {
    return addComment(postId, null, null, userId, content);
  }

  public Map<String, Object> addComment(Long postId, Long parentId, String replyToName, Long userId, String content) {
    Map<String, Object> post = postRepository.findById(postId);
    if (post == null) {
      throw new RuntimeException("帖子不存在");
    }
    String userName = userRepository.findById(userId)
        .map(u -> (String) u.get("nickname"))
        .orElse("");
    Map<String, Object> comment = commentRepository.save(postId, parentId, replyToName, userId, userName, content);
    // 评论需要审核，审核通过后才增加计数
    logger.info("用户 {} 在帖子 {} 发表评论: {}", userId, postId, content);

    // 发送评论通知给帖子作者
    try {
      Long postAuthorId = ((Number) post.get("userId")).longValue();
      logger.info("准备发送评论通知: 评论者={}, 帖子作者={}, 帖子ID={}", userId, postAuthorId, postId);

      if (!postAuthorId.equals(userId)) { // 不给自己发通知
        String postTitle = (String) post.get("title");
        String notificationContent = replyToName != null
            ? String.format("%s 回复了你的帖子《%s》: %s", userName, postTitle, content)
            : String.format("%s 评论了你的帖子《%s》: %s", userName, postTitle, content);

        notificationService.sendNotification(
            postAuthorId,
            "新评论通知",
            notificationContent,
            "CIRCLE",
            String.valueOf(postId),
            "CIRCLE"
        );
        logger.info("✅ 已向用户 {} 发送评论通知", postAuthorId);
      } else {
        logger.info("跳过通知: 评论者({})是帖子作者本人", userId);
      }
    } catch (Exception e) {
      logger.error("❌ 发送评论通知失败: {}", e.getMessage(), e);
    }

    return comment;
  }

  /**
   * 获取评论树形结构（支持无限层级嵌套）
   * 采用"一次查询 + 内存构建树"策略，性能优异
   */
  public List<Map<String, Object>> getComments(Long postId, int page, int size) {
    // 1. 获取顶级评论（用于分页）
    List<Map<String, Object>> topLevelComments = commentRepository.findTopLevelCommentsPaged(postId, "APPROVED", page, size);

    if (topLevelComments.isEmpty()) {
      return topLevelComments;
    }

    // 2. 一次性查询该帖子所有已审核评论
    List<Map<String, Object>> allComments = commentRepository.findAllByPostIdAndStatus(postId, "APPROVED");

    // 3. 构建树形结构
    return buildCommentTree(topLevelComments, allComments);
  }

  public int getCommentCount(Long postId) {
    return commentRepository.countByPostIdAndStatus(postId, "APPROVED");
  }

  /**
   * 获取我的圈子评论
   */
  public Map<String, Object> getMyComments(Long userId, int page, int size, String status) {
    List<Map<String, Object>> comments;
    int total;

    if (status != null && !status.isEmpty()) {
      comments = commentRepository.findByUserIdAndStatus(userId, status, page, size);
      total = commentRepository.countByUserIdAndStatus(userId, status);
    } else {
      comments = commentRepository.findByUserId(userId, page, size);
      total = commentRepository.countByUserId(userId);
    }

    // 丰富评论数据，添加帖子标题
    for (Map<String, Object> comment : comments) {
      Long postId = ((Number) comment.get("postId")).longValue();
      Map<String, Object> post = postRepository.findById(postId);
      if (post != null) {
        comment.put("postTitle", post.get("title"));
      }
    }

    return Map.of("comments", comments, "total", total);
  }

  public List<Map<String, Object>> getPendingPosts(int page, int size) {
    return postRepository.findByStatus("PENDING", page, size);
  }

  /** 获取所有帖子（支持状态筛选） */
  public Map<String, Object> getAllPosts(int page, int size, String status) {
    List<Map<String, Object>> posts;
    long total;
    
    if (status != null && !status.isEmpty()) {
      posts = postRepository.findByStatus(status, page, size);
      total = postRepository.countByStatus(status);
    } else {
      posts = postRepository.findAllPaged(page, size);
      total = postRepository.countAll();
    }
    
    return Map.of(
        "posts", posts,
        "list", posts,
        "records", posts,
        "total", total,
        "totalCount", total,
        "pageNo", page,
        "pageSize", size
    );
  }

  public Map<String, Object> approvePost(Long postId) {
    Map<String, Object> post = postRepository.findById(postId);
    if (post == null) {
      throw new RuntimeException("帖子不存在");
    }
    postRepository.updateStatus(postId, "APPROVED");
    logger.info("审核通过帖子: {}", postId);
    
    // 通知帖子作者
    Long postAuthorId = ((Number) post.get("userId")).longValue();
    String postTitle = (String) post.get("title");
    notificationService.sendNotification(
        postAuthorId, "帖子审核通过",
        String.format("您的帖子《%s》已通过审核，现已发布", postTitle),
        "CIRCLE", String.valueOf(postId), "CIRCLE");
    
    return postRepository.findById(postId);
  }

  public Map<String, Object> rejectPost(Long postId, String reason) {
    Map<String, Object> post = postRepository.findById(postId);
    if (post == null) {
      throw new RuntimeException("帖子不存在");
    }
    postRepository.updateStatus(postId, "REJECTED");
    logger.info("审核拒绝帖子: {}, 原因: {}", postId, reason);
    
    // 通知帖子作者
    Long postAuthorId = ((Number) post.get("userId")).longValue();
    String postTitle = (String) post.get("title");
    notificationService.sendNotification(
        postAuthorId, "帖子审核未通过",
        String.format("您的帖子《%s》未通过审核，原因：%s", postTitle, reason != null ? reason : "无"),
        "CIRCLE", String.valueOf(postId), "CIRCLE");
    
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
    String oldStatus = (String) comment.get("status");
    commentRepository.updateStatus(commentId, "APPROVED");
    // 如果之前不是 APPROVED 状态，增加评论计数
    Long postId = ((Number) comment.get("postId")).longValue();
    if (!"APPROVED".equals(oldStatus)) {
      postRepository.incrementCommentCount(postId);
    }
    // 通知评论作者
    Long commentAuthorId = ((Number) comment.get("userId")).longValue();
    var post = postRepository.findById(postId);
    String postTitle = post != null ? (String) post.get("title") : "帖子";
    notificationService.sendNotification(
        commentAuthorId, "评论审核通过",
        String.format("您对《%s》的评论已通过审核", postTitle),
        "CIRCLE", String.valueOf(commentId), "CIRCLE");

    logger.info("审核通过评论: {}", commentId);
  }

  public void rejectComment(Long commentId) {
    Map<String, Object> comment = commentRepository.findById(commentId);
    if (comment == null) {
      throw new RuntimeException("评论不存在");
    }
    String oldStatus = (String) comment.get("status");
    commentRepository.updateStatus(commentId, "REJECTED");
    // 如果之前是 APPROVED 状态，减少评论计数
    Long postId = ((Number) comment.get("postId")).longValue();
    if ("APPROVED".equals(oldStatus)) {
      postRepository.decrementCommentCount(postId);
    }
    // 通知评论作者
    Long commentAuthorId = ((Number) comment.get("userId")).longValue();
    var post = postRepository.findById(postId);
    String postTitle = post != null ? (String) post.get("title") : "帖子";
    notificationService.sendNotification(
        commentAuthorId, "评论审核未通过",
        String.format("您对《%s》的评论未通过审核", postTitle),
        "CIRCLE", String.valueOf(commentId), "CIRCLE");

    logger.info("审核拒绝评论: {}", commentId);
  }

  public boolean toggleCommentLike(Long commentId, Long userId) {
    var existingLike = commentLikeRepository.findByCommentIdAndUserId(commentId, userId);

    if (existingLike.isPresent()) {
      commentLikeRepository.deleteByCommentIdAndUserId(commentId, userId);
      commentRepository.decrementLikeCount(commentId);
      logger.info("用户 {} 取消点赞评论: {}", userId, commentId);
      return false;
    } else {
      commentLikeRepository.save(commentId, userId);
      commentRepository.incrementLikeCount(commentId);
      logger.info("用户 {} 点赞评论: {}", userId, commentId);
      return true;
    }
  }

  public boolean checkCommentLiked(Long commentId, Long userId) {
    return commentLikeRepository.findByCommentIdAndUserId(commentId, userId).isPresent();
  }

  // ========== 评论树构建方法 ==========

  /**
   * 构建评论树形结构（支持无限层级）
   */
  private List<Map<String, Object>> buildCommentTree(
      List<Map<String, Object>> topLevelComments,
      List<Map<String, Object>> allComments) {

    // 1. 构建 parentId -> children 的映射表
    Map<Long, List<Map<String, Object>>> childrenMap = new HashMap<>();
    for (Map<String, Object> comment : allComments) {
      Object parentIdObj = comment.get("parentId");
      if (parentIdObj != null) {
        Long parentId = ((Number) parentIdObj).longValue();
        childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(comment);
      }
    }

    // 2. 为每个顶级评论递归构建子树
    List<Map<String, Object>> tree = new ArrayList<>();
    for (Map<String, Object> topLevel : topLevelComments) {
      Long topLevelId = ((Number) topLevel.get("id")).longValue();
      List<Map<String, Object>> children = buildChildrenRecursive(topLevelId, childrenMap);
      topLevel.put("replies", children);
      topLevel.put("replyCount", countAllDescendants(children));
      tree.add(topLevel);
    }

    logger.info("构建评论树完成: {} 个顶级节点", tree.size());
    return tree;
  }

  /**
   * 递归构建子评论树
   */
  private List<Map<String, Object>> buildChildrenRecursive(
      Long parentId,
      Map<Long, List<Map<String, Object>>> childrenMap) {

    List<Map<String, Object>> children = childrenMap.getOrDefault(parentId, new ArrayList<>());

    for (Map<String, Object> child : children) {
      Long childId = ((Number) child.get("id")).longValue();
      // 递归查找该节点的所有子节点
      List<Map<String, Object>> grandchildren = buildChildrenRecursive(childId, childrenMap);
      child.put("replies", grandchildren);
      child.put("replyCount", countAllDescendants(grandchildren));
    }

    return children;
  }

  /**
   * 统计某节点的所有后代节点数量（包括间接子节点）
   */
  private int countAllDescendants(List<Map<String, Object>> children) {
    int count = children.size();
    for (Map<String, Object> child : children) {
      Object repliesObj = child.get("replies");
      if (repliesObj instanceof List<?> replies && !replies.isEmpty()) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> typedReplies = (List<Map<String, Object>>) replies;
        count += countAllDescendants(typedReplies);
      }
    }
    return count;
  }
}

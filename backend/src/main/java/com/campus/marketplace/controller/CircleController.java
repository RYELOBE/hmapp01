package com.campus.marketplace.controller;

import com.campus.marketplace.service.CircleService;
import com.campus.marketplace.service.CurrentUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/circle")
public class CircleController {

  private final CircleService circleService;
  private final CurrentUserService currentUserService;

  public CircleController(CircleService circleService, CurrentUserService currentUserService) {
    this.circleService = circleService;
    this.currentUserService = currentUserService;
  }

  @GetMapping("/posts")
  public Map<String, Object> getPosts(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String tag) {
    List<Map<String, Object>> posts = circleService.getPostList(page, size, tag);
    return buildSuccessResponse(Map.of("posts", posts));
  }

  @GetMapping("/posts/{id}")
  public Map<String, Object> getPostDetail(@PathVariable Long id) {
    Map<String, Object> post = circleService.getPostDetail(id);
    return buildSuccessResponse(post);
  }

  @GetMapping("/posts/{id}/comments")
  public Map<String, Object> getComments(@PathVariable Long id,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size) {
    List<Map<String, Object>> comments = circleService.getComments(id, page, size);
    return buildSuccessResponse(Map.of("comments", comments));
  }

  @PostMapping("/posts")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> createPost(@RequestBody Map<String, String> body) {
    Long userId = currentUserService.userId();
    Map<String, Object> post = circleService.createPost(userId,
        body.get("title"),
        body.get("content"),
        body.get("images"),
        body.get("tags"));
    return buildSuccessResponse(post);
  }

  @DeleteMapping("/posts/{id}")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> deletePost(@PathVariable Long id) {
    Long userId = currentUserService.userId();
    circleService.deletePost(id, userId);
    return buildSuccessResponse(Map.of("message", "删除成功"));
  }

  @PostMapping("/posts/{id}/like")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> toggleLike(@PathVariable Long id) {
    Long userId = currentUserService.userId();
    boolean liked = circleService.toggleLike(id, userId);
    Map<String, Object> result = new HashMap<>();
    result.put("liked", liked);
    return buildSuccessResponse(result);
  }

  @PostMapping("/posts/{id}/comments")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> addComment(@PathVariable Long id,
      @RequestBody Map<String, Object> body) {
    Long userId = currentUserService.userId();
    String content = (String) body.get("content");
    Object parentIdObj = body.get("parentId");
    Long parentId = parentIdObj != null ? ((Number) parentIdObj).longValue() : null;
    String replyToName = (String) body.get("replyToName");

    Map<String, Object> comment;
    if (parentId != null) {
      comment = circleService.addComment(id, parentId, replyToName, userId, content);
    } else {
      comment = circleService.addComment(id, userId, content);
    }
    return buildSuccessResponse(comment);
  }

  @GetMapping("/my-posts")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> getMyPosts(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size) {
    Long userId = currentUserService.userId();
    List<Map<String, Object>> posts = circleService.getUserPosts(userId, page, size);
    return buildSuccessResponse(Map.of("posts", posts));
  }

  @GetMapping("/pending")
  @PreAuthorize("hasRole('OPS')")
  public Map<String, Object> getPendingPosts(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size) {
    List<Map<String, Object>> posts = circleService.getPendingPosts(page, size);
    long pendingCount = circleService.getPendingCount();
    Map<String, Object> data = new HashMap<>();
    data.put("posts", posts);
    data.put("totalCount", pendingCount);
    return buildSuccessResponse(data);
  }

  @PostMapping("/posts/list")
  @PreAuthorize("hasRole('OPS')")
  public Map<String, Object> getAllPostsPost(
      @RequestBody(required = false) Map<String, Object> params) {
    int page = params != null && params.get("pageNo") != null ?
        ((Number) params.get("pageNo")).intValue() : 1;
    int pageSize = params != null && params.get("pageSize") != null ?
        ((Number) params.get("pageSize")).intValue() : 10;
    String status = params != null ? (String) params.get("status") : null;
    Map<String, Object> data = circleService.getAllPosts(page, pageSize, status);
    return buildSuccessResponse(data);
  }

  /** 获取待审核评论列表 */
  @GetMapping("/comments/pending")
  @PreAuthorize("hasRole('OPS')")
  public Map<String, Object> getPendingComments(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size) {
    List<Map<String, Object>> comments = circleService.getPendingComments(page, size);
    long pendingCount = circleService.getPendingCommentCount();
    Map<String, Object> data = new HashMap<>();
    data.put("comments", comments);
    data.put("totalCount", pendingCount);
    return buildSuccessResponse(data);
  }

  /** POST版本：获取待审核评论（兼容前端调用） */
  @PostMapping("/comments/pending")
  @PreAuthorize("hasRole('OPS')")
  public Map<String, Object> getPendingCommentsPost(
      @RequestBody(required = false) Map<String, Object> params) {
    int page = params != null && params.get("pageNo") != null ? 
        ((Number) params.get("pageNo")).intValue() : 1;
    int pageSize = params != null && params.get("pageSize") != null ? 
        ((Number) params.get("pageSize")).intValue() : 10;
    
    List<Map<String, Object>> comments = circleService.getPendingComments(page, pageSize);
    long pendingCount = circleService.getPendingCommentCount();
    Map<String, Object> data = new HashMap<>();
    data.put("comments", comments);
    data.put("totalCount", pendingCount);
    return buildSuccessResponse(data);
  }

  /** POST版本：获取已审核评论列表 */
  @PostMapping("/comments/list")
  @PreAuthorize("hasRole('OPS')")
  public Map<String, Object> getReviewedCommentsPost(
      @RequestBody(required = false) Map<String, Object> params) {
    int page = params != null && params.get("pageNo") != null ? 
        ((Number) params.get("pageNo")).intValue() : 1;
    int pageSize = params != null && params.get("pageSize") != null ? 
        ((Number) params.get("pageSize")).intValue() : 10;
    
    List<Map<String, Object>> comments = circleService.getReviewedComments(page, pageSize);
    long totalCount = circleService.getReviewedCommentCount();
    Map<String, Object> data = new HashMap<>();
    data.put("comments", comments);
    data.put("totalCount", totalCount);
    return buildSuccessResponse(data);
  }

  @PostMapping("/posts/{id}/approve")
  @PreAuthorize("hasRole('OPS')")
  public Map<String, Object> approvePost(@PathVariable Long id) {
    Map<String, Object> post = circleService.approvePost(id);
    return buildSuccessResponse(post);
  }

  @PostMapping("/posts/{id}/reject")
  @PreAuthorize("hasRole('OPS')")
  public Map<String, Object> rejectPost(@PathVariable Long id,
      @RequestBody Map<String, String> body) {
    Map<String, Object> post = circleService.rejectPost(id, body.get("reason"));
    return buildSuccessResponse(post);
  }

  /** 审核通过评论 */
  @PostMapping("/comments/{id}/approve")
  @PreAuthorize("hasRole('OPS')")
  public Map<String, Object> approveComment(@PathVariable Long id) {
    circleService.approveComment(id);
    return buildSuccessResponse(Map.of("message", "评论已通过审核"));
  }

  /** 审核拒绝/删除评论 */
  @PostMapping("/comments/{id}/reject")
  @PreAuthorize("hasRole('OPS')")
  public Map<String, Object> rejectComment(@PathVariable Long id) {
    circleService.rejectComment(id);
    return buildSuccessResponse(Map.of("message", "评论已删除"));
  }

  /** 评论点赞/取消点赞 */
  @PostMapping("/comments/{id}/like")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> toggleCommentLike(@PathVariable Long id) {
    Long userId = currentUserService.userId();
    boolean liked = circleService.toggleCommentLike(id, userId);
    Map<String, Object> result = new HashMap<>();
    result.put("liked", liked);
    return buildSuccessResponse(result);
  }

  /** 检查评论是否已点赞 */
  @GetMapping("/comments/{id}/like-status")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> checkCommentLiked(@PathVariable Long id) {
    Long userId = currentUserService.userId();
    boolean liked = circleService.checkCommentLiked(id, userId);
    Map<String, Object> result = new HashMap<>();
    result.put("liked", liked);
    return buildSuccessResponse(result);
  }

  private Map<String, Object> buildSuccessResponse(Object data) {
    return Map.of("code", 200, "data", data);
  }
}

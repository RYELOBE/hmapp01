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
  public Map<String, Object> deletePost(@PathVariable Long id) {
    Long userId = currentUserService.userId();
    circleService.deletePost(id, userId);
    return buildSuccessResponse(Map.of("message", "删除成功"));
  }

  @PostMapping("/posts/{id}/like")
  public Map<String, Object> toggleLike(@PathVariable Long id) {
    Long userId = currentUserService.userId();
    boolean liked = circleService.toggleLike(id, userId);
    Map<String, Object> result = new HashMap<>();
    result.put("liked", liked);
    return buildSuccessResponse(result);
  }

  @PostMapping("/posts/{id}/comments")
  public Map<String, Object> addComment(@PathVariable Long id,
      @RequestBody Map<String, String> body) {
    Long userId = currentUserService.userId();
    Map<String, Object> comment = circleService.addComment(id, userId, body.get("content"));
    return buildSuccessResponse(comment);
  }

  @GetMapping("/my-posts")
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

  private Map<String, Object> buildSuccessResponse(Object data) {
    return Map.of("code", 200, "data", data);
  }
}

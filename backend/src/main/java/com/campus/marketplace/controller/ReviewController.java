package com.campus.marketplace.controller;

import com.campus.marketplace.service.ReviewService;
import com.campus.marketplace.service.CurrentUserService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@Validated
public class ReviewController {

  private final ReviewService reviewService;
  private final CurrentUserService currentUserService;

  public ReviewController(ReviewService reviewService, CurrentUserService currentUserService) {
    this.reviewService = reviewService;
    this.currentUserService = currentUserService;
  }

  @GetMapping("/item/{itemId}")
  public Map<String, Object> getByItem(
      @PathVariable Long itemId,
      @RequestParam(defaultValue = "1") @Min(1) int page,
      @RequestParam(defaultValue = "10") @Max(50) int pageSize) {
    return reviewService.getReviewsByItem(itemId, page, pageSize);
  }

  @GetMapping("/item/{itemId}/stats")
  public Map<String, Object> getStats(@PathVariable Long itemId) {
    return reviewService.getReviewStats(itemId);
  }

  @GetMapping("/order/{orderId}")
  public Map<String, Object> getByOrder(@PathVariable Long orderId) {
    Long userId = currentUserService.userId();
    return reviewService.getReviewByOrder(userId, orderId);
  }

  @PostMapping
  public Map<String, Object> create(@RequestBody @Validated ReviewRequest request) {
    Long userId = currentUserService.userId();
    return reviewService.createReview(userId, request.orderId(), request.itemId(),
        request.rating(), request.content(), request.images());
  }

  @GetMapping("/my")
  public Map<String, Object> getMyReviews(
      @RequestParam(defaultValue = "1") @Min(1) int page,
      @RequestParam(defaultValue = "10") @Max(50) int pageSize) {
    Long userId = currentUserService.userId();
    return reviewService.getMyReviews(userId, page, pageSize);
  }

  @GetMapping("/pending")
  @PreAuthorize("hasRole('OPS')")
  public Map<String, Object> getPendingReviews(
      @RequestParam(defaultValue = "1") @Min(1) int page,
      @RequestParam(defaultValue = "10") @Max(50) int pageSize) {
    return reviewService.getPendingReviews(page, pageSize);
  }

  @PostMapping("/{id}/approve")
  @PreAuthorize("hasRole('OPS')")
  public Map<String, Object> approve(@PathVariable Long id) {
    return reviewService.approveReview(id);
  }

  @PostMapping("/{id}/reject")
  @PreAuthorize("hasRole('OPS')")
  public Map<String, Object> reject(@PathVariable Long id, @RequestBody RejectRequest request) {
    if (request.reason() == null || request.reason().trim().isEmpty()) {
      throw new IllegalArgumentException("拒绝原因不能为空");
    }
    return reviewService.rejectReview(id, request.reason());
  }

  @PostMapping("/{id}/reply")
  @PreAuthorize("hasAnyRole('SELLER', 'OPS')")
  public Map<String, Object> reply(@PathVariable Long id, @RequestBody @Validated ReplyRequest request) {
    Long userId = currentUserService.userId();
    return reviewService.replyToReview(userId, id, request.content());
  }

  public record ReviewRequest(
      @NotNull(message = "订单ID不能为空") Long orderId,
      @NotNull(message = "商品ID不能为空") Long itemId,
      @Min(value = 1, message = "评分最少为1") @Max(value = 5, message = "评分最多为5") int rating,
      String content,
      String images
  ) {}

  public record ReplyRequest(
      @NotNull(message = "回复内容不能为空") String content
  ) {}

  public record RejectRequest(
      @NotNull(message = "拒绝原因不能为空") String reason
  ) {}
}

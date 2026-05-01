package com.campus.marketplace.controller;

import com.campus.marketplace.service.ReviewService;
import com.campus.marketplace.service.CurrentUserService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
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

  @PostMapping("/{id}/reply")
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
}

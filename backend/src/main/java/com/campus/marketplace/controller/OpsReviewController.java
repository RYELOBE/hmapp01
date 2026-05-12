package com.campus.marketplace.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.campus.marketplace.service.CurrentUserService;
import com.campus.marketplace.service.ReviewService;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ops/reviews")
@PreAuthorize("isAuthenticated() and hasRole('OPS')")
public class OpsReviewController {
  private final ReviewService reviewService;
  private final CurrentUserService currentUserService;

  public OpsReviewController(ReviewService reviewService, CurrentUserService currentUserService) {
    this.reviewService = reviewService;
    this.currentUserService = currentUserService;
  }

  @PostMapping
  public Map<String, Object> queue(@RequestBody(required = false) ReviewQueueRequest request) {
    ReviewQueueRequest query = request != null ? request : new ReviewQueueRequest(null, null, null, 1, 10);
    int pageNo = query.pageNo() != null && query.pageNo() > 0 ? query.pageNo() : 1;
    int pageSize = query.pageSize() != null && query.pageSize() > 0 ? Math.min(query.pageSize(), 100) : 10;
    return reviewService.getReviewQueuePaged(query.status(), query.keyword(), query.category(), pageNo, pageSize);
  }

  @PostMapping("/{itemId}/approve")
  public Map<String, Object> approve(@PathVariable Long itemId) {
    reviewService.approve(itemId, currentUserService.userId());
    return Map.of("code", 200, "message", "审核已通过");
  }

  @PostMapping("/{itemId}/reject")
  public Map<String, Object> reject(@PathVariable Long itemId, @RequestBody RejectRequest request) {
    reviewService.reject(itemId, currentUserService.userId(), request.reason());
    return Map.of("code", 200, "message", "已驳回");
  }

  @DeleteMapping("/{id}")
  public Map<String, Object> delete(@PathVariable Long id) {
    reviewService.deleteReview(id);
    return Map.of("code", 200, "message", "评价已删除");
  }

  public record ReviewQueueRequest(
      String status,
      String keyword,
      String category,
      Integer pageNo,
      Integer pageSize) {}

  public record RejectRequest(String reason) {}
}

package com.campus.marketplace.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.campus.marketplace.service.CurrentUserService;
import com.campus.marketplace.service.ReviewService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

  @GetMapping
  public Map<String, Object> queue(
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "10") int pageSize) {
    return reviewService.getReviewQueuePaged(status, pageNo, pageSize);
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

  public record RejectRequest(String reason) {}
}

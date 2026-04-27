package com.campus.marketplace.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.campus.marketplace.service.CurrentUserService;
import com.campus.marketplace.service.ReviewService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ops/reviews")
@SaCheckLogin
@SaCheckRole("OPS")
public class OpsReviewController {
  private final ReviewService reviewService;
  private final CurrentUserService currentUserService;

  public OpsReviewController(ReviewService reviewService, CurrentUserService currentUserService) {
    this.reviewService = reviewService;
    this.currentUserService = currentUserService;
  }

  @GetMapping
  public Map<String, Object> queue() {
    return Map.of("items", reviewService.getReviewQueue());
  }

  @PostMapping("/{itemId}/approve")
  public Map<String, Object> approve(@PathVariable Long itemId) {
    reviewService.approve(itemId, currentUserService.userId());
    return Map.of("message", "ok");
  }

  @PostMapping("/{itemId}/reject")
  public Map<String, Object> reject(@PathVariable Long itemId, @RequestBody RejectRequest request) {
    reviewService.reject(itemId, currentUserService.userId(), request.reason());
    return Map.of("message", "ok");
  }

  public record RejectRequest(String reason) {}
}

package com.campus.marketplace.controller;

import com.campus.marketplace.service.SellerStatsService;
import com.campus.marketplace.service.CurrentUserService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller/stats")
@Validated
public class SellerStatsController {

  private final SellerStatsService sellerStatsService;
  private final CurrentUserService currentUserService;

  public SellerStatsController(SellerStatsService sellerStatsService, CurrentUserService currentUserService) {
    this.sellerStatsService = sellerStatsService;
    this.currentUserService = currentUserService;
  }

  @GetMapping("/overview")
  public Map<String, Object> overview() {
    Long userId = currentUserService.userId();
    return sellerStatsService.getOverview(userId);
  }

  @GetMapping("/trend")
  public Map<String, Object> trend(@RequestParam(defaultValue = "7") @Min(7) @Max(30) int days) {
    Long userId = currentUserService.userId();
    return sellerStatsService.getTrend(userId, days);
  }

  @GetMapping("/ranking")
  public Map<String, Object> ranking(@RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
    Long userId = currentUserService.userId();
    return sellerStatsService.getRanking(userId, limit);
  }
}

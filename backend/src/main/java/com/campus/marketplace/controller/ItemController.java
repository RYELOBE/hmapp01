package com.campus.marketplace.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.campus.marketplace.service.CurrentUserService;
import com.campus.marketplace.service.ItemService;
import com.campus.marketplace.service.ItemStatsService;
import com.campus.marketplace.service.ReviewService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
@Validated
public class ItemController {
  private final ItemService itemService;
  private final CurrentUserService currentUserService;
  private final ItemStatsService itemStatsService;
  private final ReviewService reviewService;

  public ItemController(ItemService itemService, CurrentUserService currentUserService, ItemStatsService itemStatsService, ReviewService reviewService) {
    this.itemService = itemService;
    this.currentUserService = currentUserService;
    this.itemStatsService = itemStatsService;
    this.reviewService = reviewService;
  }

  @PostMapping
  @PreAuthorize("hasRole('SELLER')")
  public Map<String, Object> createItem(@RequestBody @Validated CreateItemRequest request) {
    return itemService.createItem(
        currentUserService.userId(),
        request.title(),
        request.price(),
        request.description(),
        request.imageUrls(),
        request.category(),
        request.conditionLevel(),
        null,
        request.reviewStatus()
    );
  }

  @PostMapping("/list")
  public Map<String, Object> listItemsPost(@RequestBody(required = false) ItemQueryRequest request) {
    if (request == null) {
      request = new ItemQueryRequest(null, null, null, null, null, null, null, null, 1, 20);
    }
    Long userId = null;
    try {
      userId = currentUserService.userId();
    } catch (Exception e) {
    }

    return itemService.listItemsPaged(
        request.approvedOnly(), request.mine(), request.keyword(),
        request.category(), request.sort(), request.conditionLevel(),
        request.campus(), request.status(), request.pageNo(), request.pageSize(), userId);
  }

  @GetMapping
  public Map<String, Object> listItems(
      @RequestParam(required = false) Boolean approvedOnly,
      @RequestParam(required = false) Boolean mine,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String sort,
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize) {
    Long userId = null;
    try {
      userId = currentUserService.userId();
    } catch (Exception e) {
    }
    
    if (pageNo > 0 && pageSize > 0) {
      return itemService.listItemsPaged(
          approvedOnly, mine, keyword, category, sort,
          pageNo, pageSize, userId);
    }
    return Map.of(
        "code", 200,
        "data", itemService.listItems(approvedOnly, mine, userId));
  }

  @GetMapping("/mine")
  public Map<String, Object> myItems(
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "10") int pageSize) {
    return itemService.listMyItemsPaged(currentUserService.userId(), status, pageNo, pageSize);
  }

  @PostMapping("/mine")
  public Map<String, Object> myItemsPost(@RequestBody(required = false) MineItemRequest request) {
    if (request == null) {
      request = new MineItemRequest(null, 1, 10);
    }
    return itemService.listMyItemsPaged(currentUserService.userId(), request.status(), request.pageNo(), request.pageSize());
  }

  @GetMapping("/{id}")
  public Map<String, Object> detail(@PathVariable("id") Long id) {
    Map<String, Object> item = itemService.getItemDetail(id);
    itemStatsService.trackView(id);
    Object viewCount = item.get("viewCount");
    if (viewCount instanceof Number count) {
      item.put("viewCount", count.intValue() + 1);
    }
    return item;
  }

  @PostMapping("/{id}/off-shelf")
  @PreAuthorize("hasRole('OPS')")
  public Map<String, Object> offShelf(@PathVariable("id") Long id) {
    itemService.offShelfItem(id, null);
    return Map.of("code", 200, "message", "已下架");
  }

  @PostMapping("/{id}/withdraw")
  @PreAuthorize("hasRole('SELLER')")
  public Map<String, Object> withdrawItem(@PathVariable("id") Long id) {
    itemService.withdrawItem(id, currentUserService.userId());
    return Map.of("code", 200, "message", "已撤回");
  }

  @PostMapping("/{id}/submit")
  @PreAuthorize("hasRole('SELLER')")
  public Map<String, Object> submitForReview(@PathVariable("id") Long id) {
    itemService.submitForReview(id, currentUserService.userId());
    return Map.of("code", 200, "message", "已提交审核");
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('SELLER')")
  public Map<String, Object> updateItem(@PathVariable("id") Long id, @RequestBody @Validated CreateItemRequest request) {
    return itemService.updateItem(
        id, currentUserService.userId(),
        request.title(), request.price(), request.description(),
        request.imageUrls(), request.category(), request.conditionLevel(), request.reviewStatus());
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('SELLER')")
  public Map<String, Object> deleteItem(@PathVariable("id") Long id) {
    itemService.deleteItem(id, currentUserService.userId());
    return Map.of("code", 200, "message", "已删除");
  }

  @GetMapping("/hot")
  public Map<String, Object> getHotItems(@RequestParam(defaultValue = "8") int limit) {
    return itemStatsService.getHotItems(limit);
  }

  @PostMapping("/{id}/track-view")
  public Map<String, Object> trackView(@PathVariable("id") Long id) {
    itemStatsService.trackView(id);
    return Map.of("code", 200, "message", "ok");
  }

  @PostMapping("/{id}/track-click")
  public Map<String, Object> trackClick(@PathVariable("id") Long id) {
    itemStatsService.trackClick(id);
    return Map.of("code", 200, "message", "ok");
  }

  @PostMapping("/{id}/track-favorite")
  public Map<String, Object> trackFavorite(@PathVariable("id") Long id, @RequestParam boolean isAdd) {
    itemStatsService.trackFavorite(id, isAdd);
    return Map.of("code", 200, "message", "ok");
  }

  @PostMapping("/stats/init")
  public Map<String, Object> initStats() {
    itemStatsService.batchInitStats();
    return Map.of("code", 200, "message", "统计数据初始化完成");
  }

  @GetMapping("/{id}/reviews")
  public Map<String, Object> getItemReviews(
      @PathVariable("id") Long id,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int pageSize) {
    return reviewService.getReviewsByItem(id, page, pageSize);
  }

  @PostMapping("/{id}/reviews/query")
  public Map<String, Object> queryItemReviews(
      @PathVariable("id") Long id,
      @RequestBody(required = false) ReviewQueryRequest request) {
    if (request == null) {
      request = new ReviewQueryRequest(1, 10, null);
    }
    return reviewService.queryReviewsByItem(id, request.page(), request.pageSize(), request.status());
  }

  @PostMapping("/{id}/reviews")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> createItemReview(
      @PathVariable("id") Long id,
      @RequestBody @Validated ItemReviewRequest request) {
    Long userId = currentUserService.userId();
    
    String imagesJson = null;
    if (request.images() != null && !request.images().isEmpty()) {
      try {
        imagesJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(request.images());
      } catch (Exception e) {
        throw new IllegalArgumentException("图片数据格式错误");
      }
    }
    
    return reviewService.createReview(userId, null, id, request.rating(), request.content(), imagesJson);
  }

  public record CreateItemRequest(
      @NotBlank String title,
      @NotNull @Min(1) Integer price,
      @NotBlank String description,
      Object imageUrls,
      String category,
      String conditionLevel,
      String reviewStatus) {}

  public record ItemQueryRequest(
      Boolean approvedOnly,
      Boolean mine,
      String keyword,
      String category,
      String sort,
      String conditionLevel,
      String campus,
      String status,
      @Min(1) Integer pageNo,
      @Min(1) Integer pageSize) {}

  public record ReviewQueryRequest(
      @Min(1) Integer page,
      @Min(1) Integer pageSize,
      String status) {}

  public record ItemReviewRequest(
      @Min(value = 1, message = "评分最少为1") 
      @Max(value = 5, message = "评分最多为5") int rating,
      String content,
      java.util.List<String> images) {}

  public record MineItemRequest(
      String status,
      @Min(1) Integer pageNo,
      @Min(1) Integer pageSize) {}
}

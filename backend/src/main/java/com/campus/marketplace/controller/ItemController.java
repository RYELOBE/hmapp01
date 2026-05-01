package com.campus.marketplace.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.campus.marketplace.service.CurrentUserService;
import com.campus.marketplace.service.ItemService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
@SaCheckLogin
public class ItemController {
  private final ItemService itemService;
  private final CurrentUserService currentUserService;

  public ItemController(ItemService itemService, CurrentUserService currentUserService) {
    this.itemService = itemService;
    this.currentUserService = currentUserService;
  }

  @PostMapping
  @SaCheckRole("SELLER")
  public Map<String, Object> createItem(@RequestBody CreateItemRequest request) {
    return itemService.createItem(
        currentUserService.userId(),
        request.title(),
        request.price(),
        request.description(),
        request.imageUrls(),
        request.category(),
        request.conditionLevel(),
        null // campus
    );
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
    // 如果有分页参数，走分页查询
    if (pageNo > 0 && pageSize > 0) {
      return itemService.listItemsPaged(
          approvedOnly, mine, keyword, category, sort,
          pageNo, pageSize, currentUserService.userId());
    }
    return Map.of(
        "code", 200,
        "data", itemService.listItems(approvedOnly, mine, currentUserService.userId()));
  }

  /** 卖家的商品列表（分页） */
  @GetMapping("/mine")
  public Map<String, Object> myItems(
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "10") int pageSize) {
    return itemService.listMyItemsPaged(currentUserService.userId(), status, pageNo, pageSize);
  }

  @GetMapping("/{id}")
  public Map<String, Object> detail(@PathVariable("id") Long id) {
    return itemService.getItemDetail(id);
  }

  /** 下架商品 */
  @PostMapping("/{id}/off-shelf")
  @SaCheckRole("SELLER")
  public Map<String, Object> offShelf(@PathVariable("id") Long id) {
    itemService.offShelfItem(id, currentUserService.userId());
    return Map.of("code", 200, "message", "已下架");
  }

  public record CreateItemRequest(
      @NotBlank String title,
      @NotNull @Min(1) Integer price,
      @NotBlank String description,
      Object imageUrls, // 前端传 string[] 或 JSON 字符串均可
      String category,
      String conditionLevel) {
  }
}

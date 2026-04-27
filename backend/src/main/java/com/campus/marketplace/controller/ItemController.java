package com.campus.marketplace.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.campus.marketplace.service.CurrentUserService;
import com.campus.marketplace.service.ItemService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
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
      request.description()
    );
  }

  @GetMapping
  public Map<String, Object> listItems(
    @RequestParam(required = false) Boolean approvedOnly,
    @RequestParam(required = false) Boolean mine
  ) {
    List<Map<String, Object>> items = itemService.listItems(approvedOnly, mine, currentUserService.userId());
    return Map.of("items", items);
  }

  @GetMapping("/{id}")
  public Map<String, Object> detail(@PathVariable("id") Long id) {
    return itemService.getItemDetail(id);
  }

  public record CreateItemRequest(
    @NotBlank String title,
    @NotNull @Min(1) Integer price,
    @NotBlank String description
  ) {}
}

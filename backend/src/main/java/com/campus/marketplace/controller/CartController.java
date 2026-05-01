package com.campus.marketplace.controller;

import com.campus.marketplace.service.CartService;
import com.campus.marketplace.service.CurrentUserService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Validated
public class CartController {

  private final CartService cartService;
  private final CurrentUserService currentUserService;

  public CartController(CartService cartService, CurrentUserService currentUserService) {
    this.cartService = cartService;
    this.currentUserService = currentUserService;
  }

  @GetMapping
  public Map<String, Object> list() {
    Long userId = currentUserService.userId();
    return cartService.getCartList(userId);
  }

  @GetMapping("/count")
  public Map<String, Object> count() {
    Long userId = currentUserService.userId();
    return cartService.getCartCount(userId);
  }

  @PostMapping
  public Map<String, Object> add(@RequestBody @Validated CartAddRequest request) {
    Long userId = currentUserService.userId();
    return cartService.addToCart(userId, request.itemId(), request.quantity());
  }

  @PutMapping("/{id}")
  public Map<String, Object> updateQuantity(@PathVariable Long id, @RequestBody @Validated QuantityUpdateRequest request) {
    Long userId = currentUserService.userId();
    return cartService.updateQuantity(userId, id, request.quantity());
  }

  @PutMapping("/{id}/select")
  public Map<String, Object> updateSelected(@PathVariable Long id, @RequestBody @Validated SelectedUpdateRequest request) {
    Long userId = currentUserService.userId();
    return cartService.updateSelected(userId, id, request.selected());
  }

  @PutMapping("/batch/select")
  public Map<String, Object> batchUpdateSelected(@RequestBody @Validated BatchSelectRequest request) {
    Long userId = currentUserService.userId();
    return cartService.batchUpdateSelected(userId, request.cartIds(), request.selected());
  }

  @DeleteMapping("/{id}")
  public Map<String, Object> delete(@PathVariable Long id) {
    Long userId = currentUserService.userId();
    return cartService.deleteItem(userId, id);
  }

  @DeleteMapping("/clear")
  public Map<String, Object> clear() {
    Long userId = currentUserService.userId();
    return cartService.clearCart(userId);
  }

  public record CartAddRequest(
      @NotNull(message = "商品ID不能为空") Long itemId,
      @Min(value = 1, message = "数量最少为1") @Max(value = 99, message = "数量最多为99") int quantity
  ) {}

  public record QuantityUpdateRequest(
      @Min(value = 1, message = "数量最少为1") @Max(value = 99, message = "数量最多为99") int quantity
  ) {}

  public record SelectedUpdateRequest(boolean selected) {}

  public record BatchSelectRequest(List<Long> cartIds, boolean selected) {}
}

package com.campus.marketplace.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.campus.marketplace.service.CurrentUserService;
import com.campus.marketplace.service.OrderService;
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
@RequestMapping("/api/orders")
@SaCheckLogin
public class OrderController {
  private final OrderService orderService;
  private final CurrentUserService currentUserService;

  public OrderController(OrderService orderService, CurrentUserService currentUserService) {
    this.orderService = orderService;
    this.currentUserService = currentUserService;
  }

  @PostMapping
  @SaCheckRole(value = { "BUYER", "OPS" }, mode = SaMode.OR)
  public Map<String, Object> createOrder(@RequestBody CreateOrderRequest request) {
    return orderService.createOrder(currentUserService.userId(), request.itemId());
  }

  @GetMapping("/mine")
  public Map<String, Object> mine(
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "10") int pageSize
  ) {
    return orderService.getMinePaged(currentUserService.userId(), currentUserService.roles(), status, pageNo, pageSize);
  }

  /** 确认收货 */
  @PostMapping("/{id}/confirm")
  public Map<String, Object> confirm(@PathVariable("id") Long orderId) {
    orderService.confirmOrder(orderId, currentUserService.userId());
    return Map.of("code", 200, "message", "已确认收货");
  }

  public record CreateOrderRequest(Long itemId) {
  }
}

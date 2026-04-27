package com.campus.marketplace.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.campus.marketplace.service.CurrentUserService;
import com.campus.marketplace.service.OrderService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
  public Map<String, Object> mine() {
    return Map.of("orders", orderService.getMine(currentUserService.userId(), currentUserService.roles()));
  }

  public record CreateOrderRequest(Long itemId) {
  }
}

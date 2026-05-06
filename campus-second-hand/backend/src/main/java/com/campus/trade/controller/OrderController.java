package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.entity.Order;
import com.campus.trade.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Tag(name = "订单管理", description = "订单创建、查询、支付、收货等接口")
public class OrderController {

    @PostMapping
    @Operation(summary = "创建订单", description = "将商品加入购物车后创建订单")
    public Result<Order> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateOrderRequest request) {
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "订单列表", description = "获取当前用户的订单列表")
    public Result<PageResult<Order>> getOrderList(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "订单详情", description = "获取指定订单的详细信息")
    public Result<Order> getOrderDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return Result.success();
    }

    @PutMapping("/{id}/pay")
    @Operation(summary = "支付订单", description = "完成订单支付")
    public Result<Void> payOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam String paymentMethod) {
        return Result.success();
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "确认收货", description = "买家确认收到商品")
    public Result<Void> confirmReceipt(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单", description = "取消未支付的订单")
    public Result<Void> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return Result.success();
    }
}

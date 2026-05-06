package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.entity.CartItem;
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
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "购物车管理", description = "购物车商品添加、删除、修改等接口")
public class CartController {

    @GetMapping("/list")
    @Operation(summary = "购物车列表", description = "获取当前用户的购物车商品列表")
    public Result<List<CartItem>> getCartList(
            @AuthenticationPrincipal UserDetails userDetails) {
        return Result.success();
    }

    @PostMapping
    @Operation(summary = "添加到购物车", description = "将商品添加到购物车")
    public Result<Void> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddToCartRequest request) {
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新数量", description = "更新购物车中商品的数量")
    public Result<Void> updateQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品", description = "从购物车中删除商品")
    public Result<Void> removeFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return Result.success();
    }
}

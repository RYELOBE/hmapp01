package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.entity.User;
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
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户个人信息、收藏等管理接口")
public class UserController {

    @GetMapping("/profile")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    public Result<User> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return Result.success();
    }

    @PutMapping("/profile")
    @Operation(summary = "更新用户信息", description = "更新当前用户的个人信息")
    public Result<User> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        return Result.success();
    }

    @PostMapping("/verify")
    @Operation(summary = "校园认证", description = "提交校园认证材料进行实名认证")
    public Result<Void> verify(@AuthenticationPrincipal UserDetails userDetails,
                               @Valid @RequestBody CampusVerifyRequest request) {
        return Result.success();
    }

    @GetMapping("/favorites")
    @Operation(summary = "获取用户收藏", description = "获取当前用户的商品收藏列表")
    public Result<List<ProductDTO>> getFavorites(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success();
    }

    @PostMapping("/favorite")
    @Operation(summary = "添加收藏", description = "收藏指定商品")
    public Result<Void> addFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long productId) {
        return Result.success();
    }

    @DeleteMapping("/favorite/{productId}")
    @Operation(summary = "取消收藏", description = "取消对指定商品的收藏")
    public Result<Void> removeFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        return Result.success();
    }
}

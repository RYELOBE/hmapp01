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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户注册、登录、登出等认证相关接口")
public class AuthController {

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册账号")
    public Result<User> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success();
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名密码登录，返回Token")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(new LoginResponse("token", "refreshToken"));
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "退出当前登录状态")
    public Result<Void> logout(@AuthenticationPrincipal UserDetails userDetails) {
        return Result.success();
    }

    @GetMapping("/userinfo")
    @Operation(summary = "获取当前用户信息", description = "获取已登录用户的详细信息")
    public Result<User> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        return Result.success();
    }
}

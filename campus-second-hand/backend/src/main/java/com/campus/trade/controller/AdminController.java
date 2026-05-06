package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.entity.*;
import com.campus.trade.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "后台管理", description = "管理员专用接口，包括用户管理、商品审核、数据统计等")
public class AdminController {

    @GetMapping("/dashboard")
    @Operation(summary = "仪表盘数据", description = "获取系统运营统计数据")
    public Result<DashboardData> getDashboardData() {
        return Result.success();
    }

    @GetMapping("/user/list")
    @Operation(summary = "用户列表", description = "分页获取所有用户列表")
    public Result<PageResult<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.success();
    }

    @GetMapping("/product/list")
    @Operation(summary = "商品列表", description = "分页获取所有商品列表，支持状态筛选")
    public Result<PageResult<Product>> getProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId) {
        return Result.success();
    }

    @PutMapping("/product/{id}/audit")
    @Operation(summary = "审核商品", description = "审核通过或拒绝商品")
    public Result<Void> auditProduct(
            @PathVariable Long id,
            @RequestBody AuditRequest request) {
        return Result.success();
    }

    @GetMapping("/order/list")
    @Operation(summary = "订单列表", description = "分页获取所有订单列表")
    public Result<PageResult<Order>> getOrderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        return Result.success();
    }

    @GetMapping("/category/list")
    @Operation(summary = "分类列表", description = "获取所有商品分类")
    public Result<List<Category>> getCategoryList() {
        return Result.success();
    }

    @PostMapping("/category")
    @Operation(summary = "添加分类", description = "添加新的商品分类")
    public Result<Category> addCategory(@Valid @RequestBody CategoryCreateRequest request) {
        return Result.success();
    }

    @PutMapping("/category/{id}")
    @Operation(summary = "更新分类", description = "更新指定分类的信息")
    public Result<Category> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request) {
        return Result.success();
    }

    @DeleteMapping("/category/{id}")
    @Operation(summary = "删除分类", description = "删除指定的商品分类")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        return Result.success();
    }
}

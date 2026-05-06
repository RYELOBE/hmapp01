package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.entity.Product;
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
@RequestMapping("/api/product")
@RequiredArgsConstructor
@Tag(name = "商品管理", description = "商品发布、浏览、搜索等接口")
public class ProductController {

    @GetMapping("/list")
    @Operation(summary = "商品列表", description = "分页获取商品列表，支持分类筛选")
    public Result<PageResult<ProductDTO>> getProductList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "商品详情", description = "获取指定商品的详细信息")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        return Result.success();
    }

    @PostMapping
    @Operation(summary = "发布商品", description = "发布新的二手商品")
    public Result<Product> publishProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProductPublishRequest request) {
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新商品", description = "更新已发布的商品信息")
    public Result<Product> updateProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品", description = "删除已发布的商品")
    public Result<Void> deleteProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return Result.success();
    }

    @GetMapping("/recommend")
    @Operation(summary = "推荐商品", description = "获取个性化推荐商品列表")
    public Result<List<ProductDTO>> getRecommendProducts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success();
    }

    @GetMapping("/search")
    @Operation(summary = "搜索商品", description = "关键词搜索商品")
    public Result<PageResult<ProductDTO>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success();
    }
}

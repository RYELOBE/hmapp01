package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.entity.Category;
import com.campus.trade.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@Tag(name = "分类管理", description = "商品分类相关接口")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/list")
    @Operation(summary = "获取所有分类", description = "获取商品的全部分类列表")
    public Result<List<Category>> getCategoryList() {
        List<Category> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }
}

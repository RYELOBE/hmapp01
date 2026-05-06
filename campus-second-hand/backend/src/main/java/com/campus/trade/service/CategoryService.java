package com.campus.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.dto.CategoryDTO;
import com.campus.trade.dto.PageDTO;
import com.campus.trade.entity.Category;
import com.campus.trade.vo.Result;

import java.util.List;

public interface CategoryService extends BaseService<Category> {
    Result<List<Category>> getAllEnabled();
    Result<Boolean> addCategory(CategoryDTO dto);
    Result<Boolean> updateCategory(Long id, CategoryDTO dto);
}

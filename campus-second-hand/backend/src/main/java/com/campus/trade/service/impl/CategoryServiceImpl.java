package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.dto.CategoryDTO;
import com.campus.trade.dto.PageDTO;
import com.campus.trade.entity.Category;
import com.campus.trade.mapper.CategoryMapper;
import com.campus.trade.service.CategoryService;
import com.campus.trade.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public Page<Category> getPage(PageDTO dto) {
        Page<Category> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSortOrder);
        return this.page(page, wrapper);
    }

    @Override
    public Category getById(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean save(Category entity) {
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return this.save(entity);
    }

    @Override
    public boolean updateById(Category entity) {
        entity.setUpdateTime(LocalDateTime.now());
        return this.updateById(entity);
    }

    @Override
    public boolean deleteById(Long id) {
        return this.removeById(id);
    }

    @Override
    public Result<List<Category>> getAllEnabled() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getEnabled, true)
                .orderByAsc(Category::getSortOrder);
        List<Category> categories = this.list(wrapper);
        return Result.success(categories);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> addCategory(CategoryDTO dto) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName, dto.getName());
        if (this.count(wrapper) > 0) {
            return Result.error("分类名称已存在");
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setIcon(dto.getIcon());
        category.setDescription(dto.getDescription());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        category.setEnabled(true);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());

        return Result.success(this.save(category));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> updateCategory(Long id, CategoryDTO dto) {
        Category category = this.getById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }

        if (dto.getName() != null) {
            LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Category::getName, dto.getName())
                    .ne(Category::getId, id);
            if (this.count(wrapper) > 0) {
                return Result.error("分类名称已存在");
            }
            category.setName(dto.getName());
        }

        if (dto.getIcon() != null) {
            category.setIcon(dto.getIcon());
        }
        if (dto.getDescription() != null) {
            category.setDescription(dto.getDescription());
        }
        if (dto.getSortOrder() != null) {
            category.setSortOrder(dto.getSortOrder());
        }
        if (dto.getEnabled() != null) {
            category.setEnabled(dto.getEnabled());
        }

        category.setUpdateTime(LocalDateTime.now());
        return Result.success(this.updateById(category));
    }
}

package com.campus.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.dto.PageDTO;

public interface BaseService<T> {
    Page<T> getPage(PageDTO dto);
    T getById(Long id);
    boolean save(T entity);
    boolean updateById(T entity);
    boolean deleteById(Long id);
}

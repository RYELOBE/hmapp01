package com.campus.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.entity.Favorite;
import com.campus.trade.vo.ProductVO;
import com.campus.trade.vo.Result;
import com.campus.trade.dto.PageDTO;

import java.util.List;

public interface FavoriteService extends BaseService<Favorite> {
    Result<List<ProductVO>> getUserFavorites(Long userId);
    Result<Boolean> addFavorite(Long userId, Long productId);
    Result<Boolean> removeFavorite(Long userId, Long productId);
    Result<Boolean> isFavorited(Long userId, Long productId);
}

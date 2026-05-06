package com.campus.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.entity.Cart;
import com.campus.trade.vo.CartVO;
import com.campus.trade.vo.Result;
import com.campus.trade.dto.PageDTO;

import java.util.List;

public interface CartService extends BaseService<Cart> {
    Result<List<CartVO>> getCartList(Long userId);
    Result<Boolean> addToCart(Long userId, Long productId);
    Result<Boolean> updateQuantity(Long cartId, Long userId, Integer quantity);
    Result<Boolean> removeFromCart(Long cartId, Long userId);
    Result<Boolean> clearCart(Long userId);
}

package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.dto.PageDTO;
import com.campus.trade.entity.Cart;
import com.campus.trade.entity.Product;
import com.campus.trade.mapper.CartMapper;
import com.campus.trade.mapper.ProductMapper;
import com.campus.trade.service.CartService;
import com.campus.trade.vo.CartVO;
import com.campus.trade.vo.ProductVO;
import com.campus.trade.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    private final ProductMapper productMapper;

    @Override
    public Page<Cart> getPage(PageDTO dto) {
        Page<Cart> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Cart::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Cart getById(Long id) {
        return this.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(Cart entity) {
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return this.save(entity);
    }

    @Override
    public boolean updateById(Cart entity) {
        entity.setUpdateTime(LocalDateTime.now());
        return this.updateById(entity);
    }

    @Override
    public boolean deleteById(Long id) {
        return this.removeById(id);
    }

    @Override
    public Result<List<CartVO>> getCartList(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getCreateTime);
        List<Cart> carts = this.list(wrapper);
        List<CartVO> voList = carts.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> addToCart(Long userId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            return Result.error("商品不存在");
        }
        if (product.getStatus() != 1) {
            return Result.error("商品已下架");
        }

        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, productId);
        Cart existingCart = this.getOne(wrapper);

        if (existingCart != null) {
            LambdaUpdateWrapper<Cart> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Cart::getId, existingCart.getId())
                    .set(Cart::getQuantity, existingCart.getQuantity() + 1)
                    .set(Cart::getUpdateTime, LocalDateTime.now());
            return Result.success(this.update(updateWrapper));
        }

        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setProductId(productId);
        cart.setQuantity(1);
        cart.setCreateTime(LocalDateTime.now());
        cart.setUpdateTime(LocalDateTime.now());

        return Result.success(this.save(cart));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> updateQuantity(Long cartId, Long userId, Integer quantity) {
        Cart cart = this.getById(cartId);
        if (cart == null) {
            return Result.error("购物车项不存在");
        }
        if (!cart.getUserId().equals(userId)) {
            return Result.error("无权限修改此购物车项");
        }
        if (quantity <= 0) {
            return Result.error("数量必须大于0");
        }

        LambdaUpdateWrapper<Cart> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Cart::getId, cartId)
                .set(Cart::getQuantity, quantity)
                .set(Cart::getUpdateTime, LocalDateTime.now());

        return Result.success(this.update(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> removeFromCart(Long cartId, Long userId) {
        Cart cart = this.getById(cartId);
        if (cart == null) {
            return Result.error("购物车项不存在");
        }
        if (!cart.getUserId().equals(userId)) {
            return Result.error("无权限删除此购物车项");
        }

        return Result.success(this.removeById(cartId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> clearCart(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);

        return Result.success(this.remove(wrapper));
    }

    private CartVO convertToVO(Cart cart) {
        CartVO vo = new CartVO();
        vo.setId(cart.getId());
        vo.setUserId(cart.getUserId());
        vo.setProductId(cart.getProductId());
        vo.setQuantity(cart.getQuantity());
        vo.setCreateTime(cart.getCreateTime());
        vo.setUpdateTime(cart.getUpdateTime());

        Product product = productMapper.selectById(cart.getProductId());
        if (product != null) {
            ProductVO productVO = new ProductVO();
            productVO.setId(product.getId());
            productVO.setTitle(product.getTitle());
            productVO.setPrice(product.getPrice());
            productVO.setOriginalPrice(product.getOriginalPrice());
            productVO.setImages(product.getImages() != null ? 
                    List.of(product.getImages().split(",")) : new ArrayList<>());
            productVO.setCondition(product.getCondition());
            productVO.setStatus(product.getStatus());
            vo.setProduct(productVO);
        }

        return vo;
    }
}

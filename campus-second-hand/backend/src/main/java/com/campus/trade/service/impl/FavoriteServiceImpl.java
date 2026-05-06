package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.dto.PageDTO;
import com.campus.trade.entity.Favorite;
import com.campus.trade.entity.Product;
import com.campus.trade.entity.User;
import com.campus.trade.mapper.FavoriteMapper;
import com.campus.trade.mapper.ProductMapper;
import com.campus.trade.mapper.UserMapper;
import com.campus.trade.service.FavoriteService;
import com.campus.trade.vo.ProductVO;
import com.campus.trade.vo.Result;
import com.campus.trade.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    @Override
    public Page<Favorite> getPage(PageDTO dto) {
        Page<Favorite> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Favorite::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Favorite getById(Long id) {
        return this.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(Favorite entity) {
        entity.setCreateTime(LocalDateTime.now());
        return this.save(entity);
    }

    @Override
    public boolean updateById(Favorite entity) {
        return this.updateById(entity);
    }

    @Override
    public boolean deleteById(Long id) {
        return this.removeById(id);
    }

    @Override
    public Result<List<ProductVO>> getUserFavorites(Long userId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime);
        List<Favorite> favorites = this.list(wrapper);
        
        List<ProductVO> voList = favorites.stream()
                .map(fav -> {
                    Product product = productMapper.selectById(fav.getProductId());
                    if (product != null) {
                        return convertToVO(product);
                    }
                    return null;
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());
        
        return Result.success(voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> addFavorite(Long userId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            return Result.error("商品不存在");
        }
        if (product.getStatus() != 1) {
            return Result.error("商品已下架");
        }

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId);
        
        if (this.count(wrapper) > 0) {
            return Result.error("已收藏此商品");
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favorite.setCreateTime(LocalDateTime.now());

        LambdaUpdateWrapper<Product> productWrapper = new LambdaUpdateWrapper<>();
        productWrapper.eq(Product::getId, productId)
                .set(Product::getFavoriteCount, product.getFavoriteCount() + 1);
        productMapper.update(null, productWrapper);

        return Result.success(this.save(favorite));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> removeFavorite(Long userId, Long productId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId);
        
        Favorite favorite = this.getOne(wrapper);
        if (favorite == null) {
            return Result.error("未收藏此商品");
        }

        Product product = productMapper.selectById(productId);
        if (product != null && product.getFavoriteCount() > 0) {
            LambdaUpdateWrapper<Product> productWrapper = new LambdaUpdateWrapper<>();
            productWrapper.eq(Product::getId, productId)
                    .set(Product::getFavoriteCount, product.getFavoriteCount() - 1);
            productMapper.update(null, productWrapper);
        }

        return Result.success(this.remove(wrapper));
    }

    @Override
    public Result<Boolean> isFavorited(Long userId, Long productId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId);
        
        boolean isFavorited = this.count(wrapper) > 0;
        return Result.success(isFavorited);
    }

    private ProductVO convertToVO(Product product) {
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setUserId(product.getUserId());
        vo.setTitle(product.getTitle());
        vo.setDescription(product.getDescription());
        vo.setPrice(product.getPrice());
        vo.setOriginalPrice(product.getOriginalPrice());
        vo.setCategoryId(product.getCategoryId());
        vo.setImages(product.getImages() != null ? 
                List.of(product.getImages().split(",")) : new ArrayList<>());
        vo.setCondition(product.getCondition());
        vo.setStatus(product.getStatus());
        vo.setViewCount(product.getViewCount());
        vo.setFavoriteCount(product.getFavoriteCount());
        vo.setCreateTime(product.getCreateTime());
        vo.setUpdateTime(product.getUpdateTime());

        User user = userMapper.selectById(product.getUserId());
        if (user != null) {
            UserVO userVO = new UserVO();
            userVO.setId(user.getId());
            userVO.setUsername(user.getUsername());
            userVO.setNickname(user.getNickname());
            userVO.setAvatar(user.getAvatar());
            userVO.setCreditScore(user.getCreditScore());
            userVO.setVerified(user.getVerified());
            vo.setSeller(userVO);
        }

        return vo;
    }
}

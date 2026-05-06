package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.dto.PageDTO;
import com.campus.trade.dto.ProductPublishDTO;
import com.campus.trade.dto.ProductQueryDTO;
import com.campus.trade.dto.ProductUpdateDTO;
import com.campus.trade.entity.Product;
import com.campus.trade.entity.User;
import com.campus.trade.mapper.ProductMapper;
import com.campus.trade.service.ProductService;
import com.campus.trade.service.UserService;
import com.campus.trade.vo.ProductVO;
import com.campus.trade.vo.Result;
import com.campus.trade.vo.UserVO;
import com.campus.trade.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final UserService userService;
    private final UserMapper userMapper;

    @Override
    public Page<Product> getPage(PageDTO dto) {
        Page<Product> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
                .orderByDesc(Product::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Product getById(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean save(Product entity) {
        entity.setViewCount(0);
        entity.setFavoriteCount(0);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return this.save(entity);
    }

    @Override
    public boolean updateById(Product entity) {
        entity.setUpdateTime(LocalDateTime.now());
        return this.updateById(entity);
    }

    @Override
    public boolean deleteById(Long id) {
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> publishProduct(Long userId, ProductPublishDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Product product = new Product();
        product.setUserId(userId);
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setCategoryId(dto.getCategoryId());
        product.setImages(String.join(",", dto.getImages()));
        product.setCondition(dto.getCondition());
        product.setStatus(1);
        product.setViewCount(0);
        product.setFavoriteCount(0);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());

        if (this.save(product)) {
            return Result.success(product.getId());
        }
        return Result.error("发布失败");
    }

    @Override
    public Result<Page<ProductVO>> getProductPage(ProductQueryDTO dto) {
        Page<Product> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(Product::getStatus, 1);

        if (dto.getCategoryId() != null) {
            wrapper.eq(Product::getCategoryId, dto.getCategoryId());
        }
        if (dto.getMinPrice() != null) {
            wrapper.ge(Product::getPrice, dto.getMinPrice());
        }
        if (dto.getMaxPrice() != null) {
            wrapper.le(Product::getPrice, dto.getMaxPrice());
        }
        if (dto.getCondition() != null) {
            wrapper.eq(Product::getCondition, dto.getCondition());
        }

        if (StringUtils.hasText(dto.getSortBy())) {
            if ("price".equals(dto.getSortBy())) {
                if (Boolean.TRUE.equals(dto.getAscending())) {
                    wrapper.orderByAsc(Product::getPrice);
                } else {
                    wrapper.orderByDesc(Product::getPrice);
                }
            } else if ("createTime".equals(dto.getSortBy())) {
                wrapper.orderByDesc(Product::getCreateTime);
            } else if ("viewCount".equals(dto.getSortBy())) {
                if (Boolean.TRUE.equals(dto.getAscending())) {
                    wrapper.orderByAsc(Product::getViewCount);
                } else {
                    wrapper.orderByDesc(Product::getViewCount);
                }
            }
        } else {
            wrapper.orderByDesc(Product::getCreateTime);
        }

        Page<Product> result = this.page(page, wrapper);
        Page<ProductVO> voPage = convertToVOPage(result);
        return Result.success(voPage);
    }

    @Override
    public Result<ProductVO> getProductDetail(Long id) {
        Product product = this.getById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success(convertToVO(product));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> updateProduct(Long id, Long userId, ProductUpdateDTO dto) {
        Product product = this.getById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        if (!product.getUserId().equals(userId)) {
            return Result.error("无权限修改此商品");
        }

        if (dto.getTitle() != null) {
            product.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        if (dto.getPrice() != null) {
            product.setPrice(dto.getPrice());
        }
        if (dto.getOriginalPrice() != null) {
            product.setOriginalPrice(dto.getOriginalPrice());
        }
        if (dto.getCategoryId() != null) {
            product.setCategoryId(dto.getCategoryId());
        }
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            product.setImages(String.join(",", dto.getImages()));
        }
        if (dto.getCondition() != null) {
            product.setCondition(dto.getCondition());
        }
        if (dto.getStatus() != null) {
            product.setStatus(dto.getStatus());
        }

        product.setUpdateTime(LocalDateTime.now());
        return Result.success(this.updateById(product));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> deleteProduct(Long id, Long userId) {
        Product product = this.getById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        if (!product.getUserId().equals(userId)) {
            return Result.error("无权限删除此商品");
        }

        return Result.success(this.removeById(id));
    }

    @Override
    public Result<List<ProductVO>> getRecommendProducts(Long userId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
                .orderByDesc(Product::getViewCount, Product::getFavoriteCount)
                .last("LIMIT 10");
        List<Product> products = this.list(wrapper);
        List<ProductVO> voList = products.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    @Override
    public Result<Page<ProductVO>> searchProducts(String keyword, PageDTO pageDTO) {
        Page<Product> page = new Page<>(pageDTO.getPage(), pageDTO.getSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
                .and(w -> w.like(Product::getTitle, keyword)
                        .or()
                        .like(Product::getDescription, keyword))
                .orderByDesc(Product::getCreateTime);

        Page<Product> result = this.page(page, wrapper);
        Page<ProductVO> voPage = convertToVOPage(result);
        return Result.success(voPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> incrementViewCount(Long id) {
        LambdaUpdateWrapper<Product> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Product::getId, id)
                .set(Product::getViewCount, this.getById(id).getViewCount() + 1);
        return Result.success(this.update(wrapper));
    }

    @Override
    public Result<List<ProductVO>> getUserProducts(Long userId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getUserId, userId)
                .orderByDesc(Product::getCreateTime);
        List<Product> products = this.list(wrapper);
        List<ProductVO> voList = products.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(voList);
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

    private Page<ProductVO> convertToVOPage(Page<Product> page) {
        Page<ProductVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<ProductVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }
}

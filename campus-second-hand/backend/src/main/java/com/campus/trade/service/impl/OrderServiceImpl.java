package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.dto.OrderCreateDTO;
import com.campus.trade.dto.PageDTO;
import com.campus.trade.entity.Order;
import com.campus.trade.entity.Product;
import com.campus.trade.entity.User;
import com.campus.trade.mapper.OrderMapper;
import com.campus.trade.mapper.ProductMapper;
import com.campus.trade.mapper.UserMapper;
import com.campus.trade.service.OrderService;
import com.campus.trade.vo.OrderVO;
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
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    @Override
    public Page<Order> getPage(PageDTO dto) {
        Page<Order> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Order::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Order getById(Long id) {
        return this.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(Order entity) {
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return this.save(entity);
    }

    @Override
    public boolean updateById(Order entity) {
        entity.setUpdateTime(LocalDateTime.now());
        return this.updateById(entity);
    }

    @Override
    public boolean deleteById(Long id) {
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> createOrder(Long userId, OrderCreateDTO dto) {
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            return Result.error("商品不存在");
        }
        if (product.getStatus() != 1) {
            return Result.error("商品已下架");
        }
        if (product.getUserId().equals(userId)) {
            return Result.error("不能购买自己发布的商品");
        }

        Order order = new Order();
        order.setBuyerId(userId);
        order.setSellerId(product.getUserId());
        order.setProductId(product.getId());
        order.setQuantity(dto.getQuantity() != null ? dto.getQuantity() : 1);
        order.setTotalPrice(product.getPrice() * order.getQuantity());
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        if (this.save(order)) {
            LambdaUpdateWrapper<Product> productWrapper = new LambdaUpdateWrapper<>();
            productWrapper.eq(Product::getId, product.getId())
                    .set(Product::getStatus, 2);
            productMapper.update(null, productWrapper);
            return Result.success(order.getId());
        }
        return Result.error("创建订单失败");
    }

    @Override
    public Result<Page<OrderVO>> getUserOrders(Long userId, Integer status) {
        Page<Order> page = new Page<>(1, 20);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Order::getBuyerId, userId).or().eq(Order::getSellerId, userId));
        
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        
        wrapper.orderByDesc(Order::getCreateTime);
        Page<Order> result = this.page(page, wrapper);
        Page<OrderVO> voPage = convertToVOPage(result);
        return Result.success(voPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> payOrder(Long orderId, Long userId) {
        Order order = this.getById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            return Result.error("无权限支付此订单");
        }
        if (order.getStatus() != 0) {
            return Result.error("订单状态不正确");
        }

        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getId, orderId)
                .set(Order::getStatus, 1)
                .set(Order::getPayTime, LocalDateTime.now())
                .set(Order::getUpdateTime, LocalDateTime.now());

        return Result.success(this.update(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> confirmOrder(Long orderId, Long userId) {
        Order order = this.getById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            return Result.error("无权限确认此订单");
        }
        if (order.getStatus() != 1) {
            return Result.error("订单状态不正确");
        }

        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getId, orderId)
                .set(Order::getStatus, 2)
                .set(Order::getConfirmTime, LocalDateTime.now())
                .set(Order::getUpdateTime, LocalDateTime.now());

        User seller = userMapper.selectById(order.getSellerId());
        if (seller != null) {
            LambdaUpdateWrapper<User> userWrapper = new LambdaUpdateWrapper<>();
            userWrapper.eq(User::getId, seller.getId())
                    .set(User::getCreditScore, Math.min(100, seller.getCreditScore() + 5));
            userMapper.update(null, userWrapper);
        }

        return Result.success(this.update(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> cancelOrder(Long orderId, Long userId) {
        Order order = this.getById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            return Result.error("无权限取消此订单");
        }
        if (order.getStatus() != 0) {
            return Result.error("订单状态不正确");
        }

        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getId, orderId)
                .set(Order::getStatus, 5)
                .set(Order::getUpdateTime, LocalDateTime.now());
        this.update(wrapper);

        LambdaUpdateWrapper<Product> productWrapper = new LambdaUpdateWrapper<>();
        productWrapper.eq(Product::getId, order.getProductId())
                .set(Product::getStatus, 1);
        productMapper.update(null, productWrapper);

        return Result.success(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> applyRefund(Long orderId, Long userId, String reason) {
        Order order = this.getById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            return Result.error("无权限申请退款");
        }
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            return Result.error("订单状态不正确");
        }

        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getId, orderId)
                .set(Order::getStatus, 3)
                .set(Order::getRefundReason, reason)
                .set(Order::getUpdateTime, LocalDateTime.now());

        return Result.success(this.update(wrapper));
    }

    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setBuyerId(order.getBuyerId());
        vo.setSellerId(order.getSellerId());
        vo.setProductId(order.getProductId());
        vo.setQuantity(order.getQuantity());
        vo.setTotalPrice(order.getTotalPrice());
        vo.setStatus(order.getStatus());
        vo.setRefundReason(order.getRefundReason());
        vo.setCreateTime(order.getCreateTime());
        vo.setPayTime(order.getPayTime());
        vo.setConfirmTime(order.getConfirmTime());
        vo.setUpdateTime(order.getUpdateTime());

        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            ProductVO productVO = new ProductVO();
            productVO.setId(product.getId());
            productVO.setTitle(product.getTitle());
            productVO.setPrice(product.getPrice());
            productVO.setImages(product.getImages() != null ? 
                    List.of(product.getImages().split(",")) : new ArrayList<>());
            vo.setProduct(productVO);
        }

        User buyer = userMapper.selectById(order.getBuyerId());
        if (buyer != null) {
            UserVO buyerVO = new UserVO();
            buyerVO.setId(buyer.getId());
            buyerVO.setUsername(buyer.getUsername());
            buyerVO.setNickname(buyer.getNickname());
            buyerVO.setAvatar(buyer.getAvatar());
            vo.setBuyer(buyerVO);
        }

        User seller = userMapper.selectById(order.getSellerId());
        if (seller != null) {
            UserVO sellerVO = new UserVO();
            sellerVO.setId(seller.getId());
            sellerVO.setUsername(seller.getUsername());
            sellerVO.setNickname(seller.getNickname());
            sellerVO.setAvatar(seller.getAvatar());
            vo.setSeller(sellerVO);
        }

        return vo;
    }

    private Page<OrderVO> convertToVOPage(Page<Order> page) {
        Page<OrderVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<OrderVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }
}

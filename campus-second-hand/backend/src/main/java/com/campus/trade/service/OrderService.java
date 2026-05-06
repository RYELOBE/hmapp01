package com.campus.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.dto.OrderCreateDTO;
import com.campus.trade.entity.Order;
import com.campus.trade.vo.OrderVO;
import com.campus.trade.vo.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.trade.dto.PageDTO;

import java.util.List;

public interface OrderService extends BaseService<Order> {
    Result<Long> createOrder(Long userId, OrderCreateDTO dto);
    Result<IPage<OrderVO>> getUserOrders(Long userId, Integer status);
    Result<Boolean> payOrder(Long orderId, Long userId);
    Result<Boolean> confirmOrder(Long orderId, Long userId);
    Result<Boolean> cancelOrder(Long orderId, Long userId);
    Result<Boolean> applyRefund(Long orderId, Long userId, String reason);
}

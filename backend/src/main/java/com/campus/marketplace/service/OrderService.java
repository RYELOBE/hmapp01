package com.campus.marketplace.service;

import com.campus.marketplace.repository.ItemRepository;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
  private final OrderRepository orderRepository;
  private final ItemRepository itemRepository;
  private final UserRepository userRepository;

  public OrderService(OrderRepository orderRepository, ItemRepository itemRepository, UserRepository userRepository) {
    this.orderRepository = orderRepository;
    this.itemRepository = itemRepository;
    this.userRepository = userRepository;
  }

  public Map<String, Object> createOrder(Long buyerId, Long itemId) {
    Map<String, Object> item = itemRepository.findById(itemId);
    if (item == null) {
      throw new IllegalArgumentException("商品不存在");
    }
    Long sellerId = ((Number) item.get("sellerId")).longValue();
    String itemTitle = String.valueOf(item.get("title"));
    Integer price = ((Number) item.get("price")).intValue();

    // 获取买卖家名称
    String buyerName = userRepository.findById(buyerId).map(u -> String.valueOf(u.get("nickname"))).orElse("");
    String sellerName = String.valueOf(item.get("sellerName"));

    return orderRepository.save(buyerId, sellerId, itemId, itemTitle, price, buyerName, sellerName);
  }

  /** 根据角色返回对应订单列表 */
  public List<Map<String, Object>> getMine(Long userId, List<String> roles) {
    boolean isSeller = roles.contains("SELLER");
    boolean isBuyer = roles.contains("BUYER");
    
    if (isSeller && isBuyer) {
      List<Map<String, Object>> orders = new ArrayList<>(orderRepository.findBySellerId(userId));
      orders.addAll(orderRepository.findByBuyerId(userId));
      return orders;
    } else if (isSeller) {
      return orderRepository.findBySellerId(userId);
    } else {
      return orderRepository.findByBuyerId(userId);
    }
  }

  /** 我的订单分页（SQL 层分页） */
  public Map<String, Object> getMinePaged(Long userId, List<String> roles, String status, int pageNo, int pageSize) {
    boolean isSeller = roles.contains("SELLER");
    boolean isBuyer = roles.contains("BUYER");

    List<Map<String, Object>> rows;
    int total;

    if (isSeller && isBuyer) {
      // 双角色：合并买家+卖家的订单（各查一半再合并不太合理，优先按买家查询）
      // 实际场景中用户通常是单一角色，双角色时以买家视角为主
      rows = orderRepository.findByBuyerIdPaged(userId, status, pageNo, pageSize);
      total = orderRepository.countByBuyerIdWithFilter(userId, status);
      // TODO: 如果需要双角色完整合并，需在 SQL 层 UNION
    } else if (isSeller) {
      rows = orderRepository.findBySellerIdPaged(userId, status, pageNo, pageSize);
      total = orderRepository.countBySellerIdWithFilter(userId, status);
    } else {
      rows = orderRepository.findByBuyerIdPaged(userId, status, pageNo, pageSize);
      total = orderRepository.countByBuyerIdWithFilter(userId, status);
    }

    return Map.of(
      "code", 200,
      "data", Map.of(
        "orders", rows,
        "totalCount", total,
        "pageNo", pageNo,
        "pageSize", pageSize
      )
    );
  }

  public List<Map<String, Object>> listAllOrders() {
    return orderRepository.findAll();
  }

  public void updateOrderStatus(Long id, String status) {
    orderRepository.updateStatus(id, status);
  }

  /** 确认收货 */
  public void confirmOrder(Long orderId, Long buyerId) {
    List<Map<String, Object>> orders = orderRepository.findByBuyerId(buyerId);
    boolean found = orders.stream().anyMatch(o -> orderId.equals(o.get("id")));
    if (!found) {
      throw new IllegalArgumentException("订单不存在或不属于当前用户");
    }
    orderRepository.updateStatus(orderId, "COMPLETED");
  }
}

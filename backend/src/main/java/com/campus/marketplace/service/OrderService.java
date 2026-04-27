package com.campus.marketplace.service;

import com.campus.marketplace.repository.ItemRepository;
import com.campus.marketplace.repository.OrderRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
  private final OrderRepository orderRepository;
  private final ItemRepository itemRepository;

  public OrderService(OrderRepository orderRepository, ItemRepository itemRepository) {
    this.orderRepository = orderRepository;
    this.itemRepository = itemRepository;
  }

  public Map<String, Object> createOrder(Long buyerId, Long itemId) {
    Map<String, Object> item = itemRepository.findById(itemId);
    if (item == null) {
      throw new IllegalArgumentException("商品不存在");
    }
    Long sellerId = ((Number) item.get("sellerId")).longValue();
    String itemTitle = String.valueOf(item.get("title"));
    Integer price = ((Number) item.get("price")).intValue();
    return orderRepository.save(buyerId, sellerId, itemId, itemTitle, price);
  }

  /** 根据角色返回对应订单列表 */
  public List<Map<String, Object>> getMine(Long userId, List<String> roles) {
    if (roles.contains("SELLER")) {
      return orderRepository.findBySellerId(userId);
    }
    return orderRepository.findByBuyerId(userId);
  }

  public List<Map<String, Object>> listAllOrders() {
    return orderRepository.findAll();
  }

  public void updateOrderStatus(Long id, String status) {
    orderRepository.updateStatus(id, status);
  }
}

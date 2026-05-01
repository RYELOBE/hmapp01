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

  /** 定义状态流转规则 */
  private static final Map<String, List<String>> STATUS_TRANSITIONS = Map.of(
      "PENDING_PAYMENT", List.of("PAID", "CANCELLED"),
      "PAID", List.of("SHIPPED", "CANCELLED", "REFUNDING"),
      "SHIPPED", List.of("COMPLETED", "REFUNDING"),
      "REFUNDING", List.of("REFUNDED", "PAID"),
      "COMPLETED", List.of(),
      "CANCELLED", List.of(),
      "REFUNDED", List.of()
  );

  /** 检查状态转换是否合法 */
  private boolean canTransition(String currentStatus, String nextStatus) {
    List<String> allowed = STATUS_TRANSITIONS.get(currentStatus);
    return allowed != null && allowed.contains(nextStatus);
  }

  /** 创建订单 */
  public Map<String, Object> createOrder(Long buyerId, Long itemId, Integer quantity,
      String receiverName, String receiverPhone, String receiverAddress) {
    // 检查商品是否存在
    Map<String, Object> item = itemRepository.findById(itemId);
    if (item == null) {
      throw new IllegalArgumentException("商品不存在");
    }

    // 检查商品是否已上架
    String reviewStatus = (String) item.get("reviewStatus");
    if (!"APPROVED".equals(reviewStatus)) {
      throw new IllegalArgumentException("商品未上架");
    }

    // 检查是否有重复订单
    if (orderRepository.hasActiveOrder(buyerId, itemId)) {
      throw new IllegalArgumentException("您已有该商品的进行中订单");
    }

    // 获取买卖家信息
    Long sellerId = ((Number) item.get("sellerId")).longValue();
    String itemTitle = (String) item.get("title");
    String itemImage = (String) item.get("imageUrls");
    Integer price = ((Number) item.get("price")).intValue();
    String sellerName = (String) item.get("sellerName");
    String buyerName = userRepository.findById(buyerId).map(u -> (String) u.get("nickname")).orElse("");

    // 创建订单
    return orderRepository.save(buyerId, sellerId, itemId, itemTitle, itemImage, 
        price, quantity, receiverName, receiverPhone, receiverAddress, buyerName, sellerName);
  }

  /** 模拟支付 */
  public void pay(Long orderId, Long userId) {
    Map<String, Object> order = orderRepository.findById(orderId);
    if (order == null) {
      throw new IllegalArgumentException("订单不存在");
    }

    // 检查是否为买家
    if (!userId.equals(((Number) order.get("buyerId")).longValue())) {
      throw new IllegalArgumentException("无权操作此订单");
    }

    // 检查状态转换
    String currentStatus = (String) order.get("status");
    if (!canTransition(currentStatus, "PAID")) {
      throw new IllegalArgumentException("订单状态不允许支付");
    }

    orderRepository.updateStatus(orderId, "PAID");
  }

  /** 卖家发货 */
  public void ship(Long orderId, Long sellerId, String expressCompany, String expressNo) {
    Map<String, Object> order = orderRepository.findById(orderId);
    if (order == null) {
      throw new IllegalArgumentException("订单不存在");
    }

    // 检查是否为卖家
    if (!sellerId.equals(((Number) order.get("sellerId")).longValue())) {
      throw new IllegalArgumentException("无权操作此订单");
    }

    // 检查状态转换
    String currentStatus = (String) order.get("status");
    if (!canTransition(currentStatus, "SHIPPED")) {
      throw new IllegalArgumentException("订单状态不允许发货");
    }

    orderRepository.ship(orderId, expressCompany, expressNo);
  }

  /** 确认收货 */
  public void confirmOrder(Long orderId, Long buyerId) {
    Map<String, Object> order = orderRepository.findById(orderId);
    if (order == null) {
      throw new IllegalArgumentException("订单不存在");
    }

    // 检查是否为买家
    if (!buyerId.equals(((Number) order.get("buyerId")).longValue())) {
      throw new IllegalArgumentException("无权操作此订单");
    }

    // 检查状态转换
    String currentStatus = (String) order.get("status");
    if (!canTransition(currentStatus, "COMPLETED")) {
      throw new IllegalArgumentException("订单状态不允许确认收货");
    }

    orderRepository.updateStatus(orderId, "COMPLETED");
  }

  /** 取消订单 */
  public void cancel(Long orderId, Long userId) {
    Map<String, Object> order = orderRepository.findById(orderId);
    if (order == null) {
      throw new IllegalArgumentException("订单不存在");
    }

    // 检查是否为买家或卖家
    Long buyerId = ((Number) order.get("buyerId")).longValue();
    Long sellerId = ((Number) order.get("sellerId")).longValue();
    if (!userId.equals(buyerId) && !userId.equals(sellerId)) {
      throw new IllegalArgumentException("无权操作此订单");
    }

    // 检查状态转换
    String currentStatus = (String) order.get("status");
    if (!canTransition(currentStatus, "CANCELLED")) {
      throw new IllegalArgumentException("订单状态不允许取消");
    }

    orderRepository.updateStatus(orderId, "CANCELLED");
  }

  /** 申请退款 */
  public void requestRefund(Long orderId, Long buyerId) {
    Map<String, Object> order = orderRepository.findById(orderId);
    if (order == null) {
      throw new IllegalArgumentException("订单不存在");
    }

    // 检查是否为买家
    if (!buyerId.equals(((Number) order.get("buyerId")).longValue())) {
      throw new IllegalArgumentException("无权操作此订单");
    }

    // 检查状态转换
    String currentStatus = (String) order.get("status");
    if (!canTransition(currentStatus, "REFUNDING")) {
      throw new IllegalArgumentException("订单状态不允许申请退款");
    }

    orderRepository.updateStatus(orderId, "REFUNDING");
  }

  /** 同意退款 */
  public void approveRefund(Long orderId, Long sellerId) {
    Map<String, Object> order = orderRepository.findById(orderId);
    if (order == null) {
      throw new IllegalArgumentException("订单不存在");
    }

    // 检查是否为卖家
    if (!sellerId.equals(((Number) order.get("sellerId")).longValue())) {
      throw new IllegalArgumentException("无权操作此订单");
    }

    // 检查状态转换
    String currentStatus = (String) order.get("status");
    if (!canTransition(currentStatus, "REFUNDED")) {
      throw new IllegalArgumentException("订单状态不允许退款");
    }

    orderRepository.updateStatus(orderId, "REFUNDED");
  }

  /** 拒绝退款 */
  public void rejectRefund(Long orderId, Long sellerId) {
    Map<String, Object> order = orderRepository.findById(orderId);
    if (order == null) {
      throw new IllegalArgumentException("订单不存在");
    }

    // 检查是否为卖家
    if (!sellerId.equals(((Number) order.get("sellerId")).longValue())) {
      throw new IllegalArgumentException("无权操作此订单");
    }

    // 检查状态转换
    String currentStatus = (String) order.get("status");
    if (!"REFUNDING".equals(currentStatus)) {
      throw new IllegalArgumentException("订单状态不允许拒绝退款");
    }

    // 回退到之前的状态
    String previousStatus = "PAID"; // 默认回退到已支付
    orderRepository.updateStatus(orderId, previousStatus);
  }

  /** 获取订单详情 */
  public Map<String, Object> getOrderDetail(Long orderId, Long userId) {
    Map<String, Object> order = orderRepository.findById(orderId);
    if (order == null) {
      throw new IllegalArgumentException("订单不存在");
    }

    // 检查是否为买家或卖家
    Long buyerId = ((Number) order.get("buyerId")).longValue();
    Long sellerId = ((Number) order.get("sellerId")).longValue();
    if (!userId.equals(buyerId) && !userId.equals(sellerId)) {
      throw new IllegalArgumentException("无权查看此订单");
    }

    return order;
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
      // 双角色：优先按买家查询
      rows = orderRepository.findByBuyerIdPaged(userId, status, pageNo, pageSize);
      total = orderRepository.countByBuyerIdWithFilter(userId, status);
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
}

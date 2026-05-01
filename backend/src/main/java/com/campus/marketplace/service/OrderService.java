package com.campus.marketplace.service;

import com.campus.marketplace.repository.ItemRepository;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

  private final OrderRepository orderRepository;
  private final ItemRepository itemRepository;
  private final UserRepository userRepository;

  private static final List<String> VALID_STATUSES = List.of(
      "PENDING_PAYMENT", "PAID", "SHIPPED", "COMPLETED", "CANCELLED", "REFUNDING", "REFUNDED"
  );

  public OrderService(OrderRepository orderRepository, ItemRepository itemRepository, UserRepository userRepository) {
    this.orderRepository = orderRepository;
    this.itemRepository = itemRepository;
    this.userRepository = userRepository;
  }

  private static final Map<String, List<String>> STATUS_TRANSITIONS = Map.of(
      "PENDING_PAYMENT", List.of("PAID", "CANCELLED"),
      "PAID", List.of("SHIPPED", "CANCELLED", "REFUNDING"),
      "SHIPPED", List.of("COMPLETED", "REFUNDING"),
      "REFUNDING", List.of("REFUNDED", "PAID"),
      "COMPLETED", List.of(),
      "CANCELLED", List.of(),
      "REFUNDED", List.of()
  );

  /**
   * 检查状态转换是否合法
   */
  private boolean canTransition(String currentStatus, String nextStatus) {
    List<String> allowed = STATUS_TRANSITIONS.get(currentStatus);
    return allowed != null && allowed.contains(nextStatus);
  }

  /**
   * 创建订单
   */
  public Map<String, Object> createOrder(Long buyerId, Long itemId, Integer quantity,
      String receiverName, String receiverPhone, String receiverAddress) {
    validateBuyer(buyerId);
    validateOrderParams(itemId, quantity, receiverName, receiverPhone);

    Map<String, Object> item = itemRepository.findById(itemId);
    if (item == null) {
      throw new IllegalArgumentException("商品不存在");
    }

    String reviewStatus = (String) item.get("reviewStatus");
    if (!"APPROVED".equals(reviewStatus)) {
      throw new IllegalArgumentException("商品未上架");
    }

    if (orderRepository.hasActiveOrder(buyerId, itemId)) {
      throw new IllegalArgumentException("您已有该商品的进行中订单");
    }

    Long sellerId = ((Number) item.get("sellerId")).longValue();
    if (buyerId.equals(sellerId)) {
      throw new IllegalArgumentException("不能购买自己的商品");
    }
    String itemTitle = (String) item.get("title");
    String itemImage = "";
    Object imageUrlsObj = item.get("imageUrls");
    if (imageUrlsObj != null) {
      String imageUrlsStr = imageUrlsObj.toString();
      if (imageUrlsStr.startsWith("[")) {
        try {
          var urls = new com.fasterxml.jackson.databind.ObjectMapper().readValue(imageUrlsStr, java.util.List.class);
          if (!urls.isEmpty()) itemImage = (String) urls.get(0);
        } catch (Exception ignored) {}
      } else {
        itemImage = imageUrlsStr;
      }
    }
    Integer price = ((Number) item.get("price")).intValue();
    String sellerName = (String) item.get("sellerName");
    String buyerName = getBuyerName(buyerId);

    return orderRepository.save(buyerId, sellerId, itemId, itemTitle, itemImage,
        price, quantity, receiverName, receiverPhone, receiverAddress, buyerName, sellerName);
  }

  /**
   * 模拟支付
   */
  public void pay(Long orderId, Long userId) {
    Map<String, Object> order = validateOrderAccess(orderId, userId);
    String currentStatus = (String) order.get("status");
    if (!canTransition(currentStatus, "PAID")) {
      throw new IllegalArgumentException("订单状态不允许支付");
    }
    orderRepository.updateStatus(orderId, "PAID");
  }

  /**
   * 卖家发货
   */
  public void ship(Long orderId, Long sellerId, String expressCompany, String expressNo) {
    Map<String, Object> order = validateOrderOwnership(orderId, sellerId);
    String currentStatus = (String) order.get("status");
    if (!canTransition(currentStatus, "SHIPPED")) {
      throw new IllegalArgumentException("订单状态不允许发货");
    }
    if (expressCompany == null || expressCompany.isEmpty()) {
      throw new IllegalArgumentException("快递公司不能为空");
    }
    if (expressNo == null || expressNo.isEmpty()) {
      throw new IllegalArgumentException("快递单号不能为空");
    }
    orderRepository.ship(orderId, expressCompany, expressNo);
  }

  /**
   * 确认收货
   */
  public void confirmOrder(Long orderId, Long buyerId) {
    Map<String, Object> order = validateOrderBuyerAccess(orderId, buyerId);
    String currentStatus = (String) order.get("status");
    if (!canTransition(currentStatus, "COMPLETED")) {
      throw new IllegalArgumentException("订单状态不允许确认收货");
    }
    orderRepository.updateStatus(orderId, "COMPLETED");
  }

  /**
   * 取消订单
   */
  public void cancel(Long orderId, Long userId) {
    Map<String, Object> order = findOrderById(orderId);
    Long buyerId = ((Number) order.get("buyerId")).longValue();
    Long sellerId = ((Number) order.get("sellerId")).longValue();
    if (!userId.equals(buyerId) && !userId.equals(sellerId)) {
      throw new IllegalArgumentException("无权操作此订单");
    }

    String currentStatus = (String) order.get("status");
    if (!canTransition(currentStatus, "CANCELLED")) {
      throw new IllegalArgumentException("订单状态不允许取消");
    }
    orderRepository.updateStatus(orderId, "CANCELLED");
  }

  /**
   * 申请退款
   */
  public void requestRefund(Long orderId, Long buyerId) {
    Map<String, Object> order = validateOrderBuyerAccess(orderId, buyerId);
    String currentStatus = (String) order.get("status");
    if (!canTransition(currentStatus, "REFUNDING")) {
      throw new IllegalArgumentException("订单状态不允许申请退款");
    }
    orderRepository.updateStatus(orderId, "REFUNDING");
  }

  /**
   * 同意退款
   */
  public void approveRefund(Long orderId, Long sellerId) {
    Map<String, Object> order = validateOrderOwnership(orderId, sellerId);
    String currentStatus = (String) order.get("status");
    if (!canTransition(currentStatus, "REFUNDED")) {
      throw new IllegalArgumentException("订单状态不允许退款");
    }
    orderRepository.updateStatus(orderId, "REFUNDED");
  }

  /**
   * 拒绝退款
   */
  public void rejectRefund(Long orderId, Long sellerId) {
    Map<String, Object> order = validateOrderOwnership(orderId, sellerId);
    String currentStatus = (String) order.get("status");
    if (!"REFUNDING".equals(currentStatus)) {
      throw new IllegalArgumentException("订单状态不允许拒绝退款");
    }
    orderRepository.updateStatus(orderId, "PAID");
  }

  /**
   * 获取订单详情
   */
  public Map<String, Object> getOrderDetail(Long orderId, Long userId) {
    Map<String, Object> order = findOrderById(orderId);
    Long buyerId = ((Number) order.get("buyerId")).longValue();
    Long sellerId = ((Number) order.get("sellerId")).longValue();
    if (!userId.equals(buyerId) && !userId.equals(sellerId)) {
      throw new IllegalArgumentException("无权查看此订单");
    }
    return order;
  }

  /**
   * 根据角色返回对应订单列表
   */
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

  /**
   * 我的订单分页（SQL 层分页）
   */
  public Map<String, Object> getMinePaged(Long userId, List<String> roles, String status, int pageNo, int pageSize) {
    validatePageParams(pageNo, pageSize);
    boolean isSeller = roles.contains("SELLER");
    boolean isBuyer = roles.contains("BUYER");

    List<Map<String, Object>> rows;
    int total;

    if (isSeller && isBuyer) {
      List<Map<String, Object>> buyerRows = orderRepository.findByBuyerIdPaged(userId, status, pageNo, pageSize);
      List<Map<String, Object>> sellerRows = orderRepository.findBySellerIdPaged(userId, status, pageNo, pageSize);
      rows = new ArrayList<>(buyerRows);
      rows.addAll(sellerRows);
      rows.sort((a, b) -> {
        String timeA = String.valueOf(a.getOrDefault("createdAt", ""));
        String timeB = String.valueOf(b.getOrDefault("createdAt", ""));
        return timeB.compareTo(timeA);
      });
      int buyerTotal = orderRepository.countByBuyerIdWithFilter(userId, status);
      int sellerTotal = orderRepository.countBySellerIdWithFilter(userId, status);
      total = buyerTotal + sellerTotal;
    } else if (isSeller) {
      rows = orderRepository.findBySellerIdPaged(userId, status, pageNo, pageSize);
      total = orderRepository.countBySellerIdWithFilter(userId, status);
    } else {
      rows = orderRepository.findByBuyerIdPaged(userId, status, pageNo, pageSize);
      total = orderRepository.countByBuyerIdWithFilter(userId, status);
    }

    return buildSuccessResponse(Map.of(
        "orders", rows,
        "totalCount", total,
        "pageNo", pageNo,
        "pageSize", pageSize
    ));
  }

  public List<Map<String, Object>> listAllOrders() {
    return orderRepository.findAll();
  }

  /**
   * 统计今日订单数
   */
  public int countTodayOrders() {
    return orderRepository.countTodayOrders();
  }

  /**
   * 统计今日已完成订单数
   */
  public int countTodayCompletedOrders() {
    return orderRepository.countTodayCompletedOrders();
  }

  /**
   * 统计今日订单金额
   */
  public int sumTodayOrderAmount() {
    return orderRepository.sumTodayTotalAmount();
  }

  /**
   * 按日期范围统计订单数量
   */
  public int countOrdersByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
    if (startTime == null || endTime == null) {
      throw new IllegalArgumentException("时间范围不能为空");
    }
    return orderRepository.countByDateRange(null, startTime, endTime);
  }

  /**
   * 按日期范围统计订单金额
   */
  public int sumOrderAmountByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
    if (startTime == null || endTime == null) {
      throw new IllegalArgumentException("时间范围不能为空");
    }
    return orderRepository.sumTotalAmountByDateRange(startTime, endTime);
  }

  /**
   * 统计已完成订单总金额
   */
  public int sumCompletedOrderAmount() {
    return orderRepository.sumCompletedTotalAmount();
  }

  /**
   * 获取订单统计摘要
   */
  public Map<String, Object> getOrderStatistics() {
    Map<String, Object> stats = new HashMap<>();
    stats.put("todayOrders", orderRepository.countTodayOrders());
    stats.put("todayCompletedOrders", orderRepository.countTodayCompletedOrders());
    stats.put("todayPendingPaymentOrders", orderRepository.countTodayPendingPaymentOrders());
    stats.put("todayOrderAmount", orderRepository.sumTodayTotalAmount());
    stats.put("totalCompletedAmount", orderRepository.sumCompletedTotalAmount());
    stats.put("totalOrders", orderRepository.countAll());
    return stats;
  }

  /**
   * 验证订单访问权限（买家或卖家）
   */
  private Map<String, Object> validateOrderAccess(Long orderId, Long userId) {
    Map<String, Object> order = findOrderById(orderId);
    Long buyerId = ((Number) order.get("buyerId")).longValue();
    Long sellerId = ((Number) order.get("sellerId")).longValue();
    if (!userId.equals(buyerId) && !userId.equals(sellerId)) {
      throw new IllegalArgumentException("无权操作此订单");
    }
    return order;
  }

  /**
   * 验证订单买家权限
   */
  private Map<String, Object> validateOrderBuyerAccess(Long orderId, Long buyerId) {
    Map<String, Object> order = findOrderById(orderId);
    if (!buyerId.equals(((Number) order.get("buyerId")).longValue())) {
      throw new IllegalArgumentException("无权操作此订单");
    }
    return order;
  }

  /**
   * 验证订单卖家权限
   */
  private Map<String, Object> validateOrderOwnership(Long orderId, Long sellerId) {
    Map<String, Object> order = findOrderById(orderId);
    if (!sellerId.equals(((Number) order.get("sellerId")).longValue())) {
      throw new IllegalArgumentException("无权操作此订单");
    }
    return order;
  }

  /**
   * 查找订单
   */
  private Map<String, Object> findOrderById(Long orderId) {
    if (orderId == null) {
      throw new IllegalArgumentException("订单ID不能为空");
    }
    Map<String, Object> order = orderRepository.findById(orderId);
    if (order == null) {
      throw new IllegalArgumentException("订单不存在");
    }
    return order;
  }

  /**
   * 验证买家
   */
  private void validateBuyer(Long buyerId) {
    if (buyerId == null) {
      throw new IllegalArgumentException("买家ID不能为空");
    }
  }

  /**
   * 验证订单参数
   */
  private void validateOrderParams(Long itemId, Integer quantity, String receiverName, String receiverPhone) {
    if (itemId == null) {
      throw new IllegalArgumentException("商品ID不能为空");
    }
    if (quantity == null || quantity < 1) {
      throw new IllegalArgumentException("商品数量必须大于0");
    }
    if (receiverName == null || receiverName.isEmpty()) {
      throw new IllegalArgumentException("收货人姓名不能为空");
    }
    if (receiverPhone == null || receiverPhone.isEmpty()) {
      throw new IllegalArgumentException("联系电话不能为空");
    }
  }

  /**
   * 验证分页参数
   */
  private void validatePageParams(int pageNo, int pageSize) {
    if (pageNo < 1) {
      throw new IllegalArgumentException("页码必须大于0");
    }
    if (pageSize < 1 || pageSize > 100) {
      throw new IllegalArgumentException("每页数量必须在1-100之间");
    }
  }

  /**
   * 获取买家名称
   */
  private String getBuyerName(Long buyerId) {
    return userRepository.findById(buyerId)
        .map(u -> (String) u.get("nickname"))
        .orElse("");
  }

  /**
   * 构建成功响应
   */
  private Map<String, Object> buildSuccessResponse(Object data) {
    return Map.of("code", 200, "data", data);
  }
}

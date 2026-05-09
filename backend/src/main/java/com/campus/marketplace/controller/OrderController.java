package com.campus.marketplace.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.campus.marketplace.service.CurrentUserService;
import com.campus.marketplace.service.OrderService;
import com.campus.marketplace.service.NotificationService;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@PreAuthorize("isAuthenticated()")
public class OrderController {
  private final OrderService orderService;
  private final CurrentUserService currentUserService;
  private final NotificationService notificationService;

  public OrderController(OrderService orderService, CurrentUserService currentUserService, NotificationService notificationService) {
    this.orderService = orderService;
    this.currentUserService = currentUserService;
    this.notificationService = notificationService;
  }

  /** 创建订单 */
  @PostMapping
  @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'OPS')")
  public Map<String, Object> createOrder(@RequestBody CreateOrderRequest request) {
    Map<String, Object> order = orderService.createOrder(
        currentUserService.userId(),
        request.itemId(),
        request.quantity() != null ? request.quantity() : 1,
        request.receiverName(),
        request.receiverPhone(),
        request.receiverAddress()
    );
    return Map.of("code", 200, "data", order);
  }

  /** 获取我的订单列表 */
  @GetMapping("/mine")
  public Map<String, Object> mine(
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "10") int pageSize
  ) {
    return orderService.getMinePaged(currentUserService.userId(), currentUserService.roles(), status, pageNo, pageSize);
  }

  /** 运营端：获取所有订单列表 */
  @GetMapping
  @PreAuthorize("hasRole('OPS')")
  public Map<String, Object> list(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "15") int pageSize
  ) {
    return orderService.listForOps(status, keyword, pageNo, pageSize);
  }

  /** 获取订单详情 */
  @GetMapping("/{id}")
  public Map<String, Object> detail(@PathVariable Long id) {
    Map<String, Object> order = orderService.getOrderDetail(id, currentUserService.userId());
    return Map.of("code", 200, "data", order);
  }

  /** 模拟支付 */
  @PostMapping("/{id}/pay")
  public Map<String, Object> pay(@PathVariable Long id) {
    orderService.pay(id, currentUserService.userId());
    return Map.of("code", 200, "message", "支付成功");
  }

  /** 确认收货 */
  @PostMapping("/{id}/confirm")
  public Map<String, Object> confirm(@PathVariable Long id) {
    orderService.confirmOrder(id, currentUserService.userId());
    return Map.of("code", 200, "message", "确认收货成功");
  }

  /** 卖家发货 */
  @PostMapping("/{id}/ship")
  public Map<String, Object> ship(@PathVariable Long id, @RequestBody ShipRequest request) {
    orderService.ship(id, currentUserService.userId(), request.expressCompany(), request.expressNo());
    return Map.of("code", 200, "message", "发货成功");
  }

  /** 取消订单 */
  @PostMapping("/{id}/cancel")
  public Map<String, Object> cancel(@PathVariable Long id) {
    orderService.cancel(id, currentUserService.userId());
    return Map.of("code", 200, "message", "订单已取消");
  }

  /** 申请退款 */
  @PostMapping("/{id}/refund")
  public Map<String, Object> requestRefund(@PathVariable Long id) {
    orderService.requestRefund(id, currentUserService.userId());
    
    // 发送退款申请通知给运营人员
    try {
      notificationService.createBusinessNotification(
        "ORDER", 
        id.toString(), 
        "订单退款申请", 
        "用户申请退款，订单ID: " + id + "，请及时处理", 
        null
      );
    } catch (Exception e) {
      // 记录日志但不影响主流程
      System.err.println("Failed to send refund notification: " + e.getMessage());
    }
    
    return Map.of("code", 200, "message", "退款申请已提交");
  }

  /** 同意退款 */
  @PostMapping("/{id}/refund/approve")
  @PreAuthorize("hasRole('OPS') or hasRole('SELLER')")
  public Map<String, Object> approveRefund(@PathVariable Long id) {
    orderService.approveRefund(id, currentUserService.userId());
    return Map.of("code", 200, "message", "退款成功");
  }

  /** 拒绝退款 */
  @PostMapping("/{id}/refund/reject")
  @PreAuthorize("hasRole('OPS') or hasRole('SELLER')")
  public Map<String, Object> rejectRefund(@PathVariable Long id) {
    orderService.rejectRefund(id, currentUserService.userId());
    return Map.of("code", 200, "message", "已拒绝退款申请");
  }

  public record CreateOrderRequest(
      Long itemId,
      Integer quantity,
      String receiverName,
      String receiverPhone,
      String receiverAddress
  ) {}

  public record ShipRequest(
      String expressCompany,
      String expressNo
  ) {}
}

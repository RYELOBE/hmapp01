package com.campus.marketplace.service;

import com.campus.marketplace.repository.ItemRepository;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpsService {

  private final ItemRepository itemRepository;
  private final OrderRepository orderRepository;
  private final UserRepository userRepository;

  public OpsService(ItemRepository itemRepository, OrderRepository orderRepository, UserRepository userRepository) {
    this.itemRepository = itemRepository;
    this.orderRepository = orderRepository;
    this.userRepository = userRepository;
  }

  /**
   * 运营统计数据聚合
   * @return 包含各类统计数据的Map
   */
  public Map<String, Object> getStatistics() {
    Map<String, Object> stats = new HashMap<>();

    // 待审核商品统计
    int pendingReviews = itemRepository.countByStatus("PENDING_REVIEW");
    stats.put("pendingReviews", pendingReviews);

    // 今日统计
    stats.put("todayNewUsers", userRepository.countTodayNewUsers());
    stats.put("todayItems", itemRepository.countTodayItems());
    stats.put("todayApproved", itemRepository.countTodayApprovedItems());
    stats.put("todayRejected", itemRepository.countTodayRejectedItems());
    stats.put("todayOrders", orderRepository.countTodayOrders());
    stats.put("todayCompletedOrders", orderRepository.countTodayCompletedOrders());
    stats.put("todayPendingPaymentOrders", orderRepository.countTodayPendingPaymentOrders());
    stats.put("todayOrderAmount", orderRepository.sumTodayTotalAmount());

    // 累计统计
    stats.put("totalItems", itemRepository.countAll());
    stats.put("totalOrders", orderRepository.countAll());
    stats.put("totalUsers", userRepository.countAll());
    stats.put("totalVendors", itemRepository.countVendors(null));
    stats.put("totalBuyers", orderRepository.countBuyers(null));

    // 活跃用户统计
    stats.put("activeUsers", userRepository.countActiveUsers());

    // 订单金额统计
    stats.put("completedOrderAmount", orderRepository.sumCompletedTotalAmount());

    return stats;
  }

  /**
   * 获取简要统计数据（用于快速展示）
   * @return 简要统计数据
   */
  public Map<String, Object> getBriefStatistics() {
    Map<String, Object> stats = new HashMap<>();
    stats.put("pendingReviews", itemRepository.countByStatus("PENDING_REVIEW"));
    stats.put("todayNewUsers", userRepository.countTodayNewUsers());
    stats.put("todayItems", itemRepository.countTodayItems());
    stats.put("todayOrders", orderRepository.countTodayOrders());
    stats.put("activeUsers", userRepository.countActiveUsers());
    return stats;
  }

  /**
   * 订单分页查询
   * @param status 订单状态筛选
   * @param keyword 搜索关键词
   * @param pageNo 页码
   * @param pageSize 每页数量
   * @return 订单分页结果
   */
  public Map<String, Object> getOrders(String status, String keyword, int pageNo, int pageSize) {
    validatePageParams(pageNo, pageSize);
    List<Map<String, Object>> rows = orderRepository.findByPage(status, keyword, pageNo, pageSize);
    int total = orderRepository.countByFilter(status, keyword);
    return buildSuccessResponse(Map.of(
      "rows", rows,
      "totalCount", total,
      "pageNo", pageNo,
      "pageSize", pageSize
    ));
  }

  /**
   * 供方列表
   * @param keyword 搜索关键词
   * @param pageNo 页码
   * @param pageSize 每页数量
   * @return 供方分页结果
   */
  public Map<String, Object> getVendors(String keyword, int pageNo, int pageSize) {
    validatePageParams(pageNo, pageSize);
    List<Map<String, Object>> rows = itemRepository.findVendorsWithStats(keyword, pageNo, pageSize);
    int total = itemRepository.countVendors(keyword);
    return buildSuccessResponse(Map.of(
      "vendors", rows,
      "totalCount", total,
      "pageNo", pageNo,
      "pageSize", pageSize
    ));
  }

  /**
   * 需方列表
   * @param keyword 搜索关键词
   * @param pageNo 页码
   * @param pageSize 每页数量
   * @return 需方分页结果
   */
  public Map<String, Object> getBuyers(String keyword, int pageNo, int pageSize) {
    validatePageParams(pageNo, pageSize);
    List<Map<String, Object>> rows = orderRepository.findBuyersWithStats(keyword, pageNo, pageSize);
    int total = orderRepository.countBuyers(keyword);
    return buildSuccessResponse(Map.of(
      "buyers", rows,
      "totalCount", total,
      "pageNo", pageNo,
      "pageSize", pageSize
    ));
  }

  /**
   * 用户列表（分页）
   * @param keyword 搜索关键词
   * @param pageNo 页码
   * @param pageSize 每页数量
   * @return 用户分页结果
   */
  public Map<String, Object> getUsers(String keyword, int pageNo, int pageSize) {
    validatePageParams(pageNo, pageSize);
    List<Map<String, Object>> rows = userRepository.findByPage(keyword, pageNo, pageSize);
    int total = userRepository.countAll(keyword);
    return buildSuccessResponse(Map.of(
      "users", rows,
      "totalCount", total,
      "pageNo", pageNo,
      "pageSize", pageSize
    ));
  }

  /**
   * 获取用户详情
   * @param userId 用户ID
   * @return 用户详情
   */
  public Map<String, Object> getUserDetail(Long userId) {
    if (userId == null) {
      throw new IllegalArgumentException("用户ID不能为空");
    }
    return userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
  }

  /**
   * 更新用户角色
   * @param userId 用户ID
   * @param roles 新角色列表
   */
  public void updateUserRoles(Long userId, List<String> roles) {
    if (userId == null) {
      throw new IllegalArgumentException("用户ID不能为空");
    }
    if (roles == null || roles.isEmpty()) {
      throw new IllegalArgumentException("角色列表不能为空");
    }
    userRepository.updateRoles(userId, roles);
  }

  public void updateUserStatus(Long userId, String status) {
    if (userId == null) {
      throw new IllegalArgumentException("用户ID不能为空");
    }
    if (status == null || status.isBlank()) {
      throw new IllegalArgumentException("状态不能为空");
    }
    userRepository.updateStatus(userId, status);
  }

  /**
   * 获取待审核商品列表（分页）
   * @param pageNo 页码
   * @param pageSize 每页数量
   * @return 待审核商品分页结果
   */
  public Map<String, Object> getPendingItems(int pageNo, int pageSize) {
    validatePageParams(pageNo, pageSize);
    List<Map<String, Object>> rows = itemRepository.findPendingByPage(pageNo, pageSize);
    int total = itemRepository.countPending();
    return buildSuccessResponse(Map.of(
      "items", rows,
      "totalCount", total,
      "pageNo", pageNo,
      "pageSize", pageSize
    ));
  }

  /**
   * 参数校验
   * @param pageNo 页码
   * @param pageSize 每页数量
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
   * 构建成功响应
   * @param data 响应数据
   * @return 统一格式响应
   */
  private Map<String, Object> buildSuccessResponse(Object data) {
    return Map.of("code", 200, "data", data);
  }
}

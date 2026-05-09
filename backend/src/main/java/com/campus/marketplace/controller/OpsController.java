package com.campus.marketplace.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.campus.marketplace.service.CircleService;
import com.campus.marketplace.service.ItemService;
import com.campus.marketplace.service.OpsService;
import com.campus.marketplace.service.ReviewService;
import com.campus.marketplace.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ops")
@PreAuthorize("isAuthenticated() and hasRole('OPS')")
public class OpsController {

  private final OpsService opsService;
  private final StatsService statsService;
  private final CircleService circleService;
  private final ReviewService reviewService;
  private final ItemService itemService;

  public OpsController(OpsService opsService, StatsService statsService, CircleService circleService, ReviewService reviewService, ItemService itemService) {
    this.opsService = opsService;
    this.statsService = statsService;
    this.circleService = circleService;
    this.reviewService = reviewService;
    this.itemService = itemService;
  }

  /**
   * 运营统计数据聚合
   * @return 完整统计数据
   */
  @GetMapping("/statistics")
  public Map<String, Object> getStatistics() {
    return buildSuccessResponse(Map.of("statistics", opsService.getStatistics()));
  }

  /**
   * 获取简要统计数据（用于快速展示）
   * @return 简要统计数据
   */
  @GetMapping("/statistics/brief")
  public Map<String, Object> getBriefStatistics() {
    return buildSuccessResponse(Map.of("statistics", opsService.getBriefStatistics()));
  }

  /**
   * 获取完整统计数据（包含圈子系统）
   * @return 完整统计数据
   */
  @GetMapping("/stats")
  public Map<String, Object> getStats() {
    return buildSuccessResponse(statsService.getAllStats());
  }

  /**
   * 获取简要统计数据（包含圈子系统）
   * @return 简要统计数据
   */
  @GetMapping("/stats/brief")
  public Map<String, Object> getBriefStats() {
    return buildSuccessResponse(statsService.getBriefStats());
  }

  /**
   * 获取近7天订单趋势
   * @return 订单趋势数据
   */
  @GetMapping("/stats/order-trend")
  public Map<String, Object> getOrderTrend() {
    return buildSuccessResponse(Map.of("trend", statsService.getOrderTrend()));
  }

  /**
   * 获取商品分类统计
   * @return 分类占比数据
   */
  @GetMapping("/stats/categories")
  public Map<String, Object> getCategoryStats() {
    return buildSuccessResponse(Map.of("categories", statsService.getCategoryStats()));
  }

  /**
   * 获取订单状态分布
   * @return 状态分布数据
   */
  @GetMapping("/stats/order-distribution")
  public Map<String, Object> getOrderStatusDistribution() {
    return buildSuccessResponse(statsService.getOrderStatusDistribution());
  }

  /**
   * 获取最近活动记录
   * @param limit 返回数量（默认10，最大50）
   * @return 最近活动列表
   */
  @GetMapping("/stats/activities")
  public Map<String, Object> getRecentActivities(@RequestParam(defaultValue = "10") Integer limit) {
    int safeLimit = Math.min(Math.max(limit, 1), 50);
    return buildSuccessResponse(Map.of("activities", statsService.getRecentActivities(safeLimit)));
  }

  /**
   * 订单分页查询
   * @param status 订单状态筛选
   * @param keyword 搜索关键词
   * @param pageNo 页码（默认1）
   * @param pageSize 每页数量（默认10，最大100）
   * @return 订单分页结果
   */
  @PostMapping("/orders")
  public Map<String, Object> getOrders(@RequestBody(required = false) OpsListRequest request) {
    OpsListRequest query = request != null ? request : new OpsListRequest(null, null, null, null, null);
    return opsService.getOrders(query.status(), query.keyword(), pageNo(query), pageSize(query));
  }

  /**
   * 供方列表
   * @param keyword 搜索关键词
   * @param pageNo 页码（默认1）
   * @param pageSize 每页数量（默认10，最大100）
   * @return 供方分页结果
   */
  @PostMapping("/vendors")
  public Map<String, Object> getVendors(@RequestBody(required = false) OpsListRequest request) {
    OpsListRequest query = request != null ? request : new OpsListRequest(null, null, null, null, null);
    return opsService.getVendors(query.keyword(), pageNo(query), pageSize(query));
  }

  /**
   * 需方列表
   * @param keyword 搜索关键词
   * @param pageNo 页码（默认1）
   * @param pageSize 每页数量（默认10，最大100）
   * @return 需方分页结果
   */
  @PostMapping("/buyers")
  public Map<String, Object> getBuyers(@RequestBody(required = false) OpsListRequest request) {
    OpsListRequest query = request != null ? request : new OpsListRequest(null, null, null, null, null);
    return opsService.getBuyers(query.keyword(), pageNo(query), pageSize(query));
  }

  @GetMapping("/buyers/{buyerId}")
  public Map<String, Object> getBuyerDetail(@PathVariable Long buyerId) {
    return buildSuccessResponse(opsService.getUserDetail(buyerId));
  }

  @GetMapping("/vendors/{vendorId}")
  public Map<String, Object> getVendorDetail(@PathVariable Long vendorId) {
    return buildSuccessResponse(opsService.getUserDetail(vendorId));
  }

  /**
   * 用户列表（分页）
   * @param keyword 搜索关键词
   * @param pageNo 页码（默认1）
   * @param pageSize 每页数量（默认10，最大100）
   * @return 用户分页结果
   */
  @PostMapping("/users")
  public Map<String, Object> getUsers(@RequestBody(required = false) OpsListRequest request) {
    OpsListRequest query = request != null ? request : new OpsListRequest(null, null, null, null, null);
    return opsService.getUsers(query.keyword(), pageNo(query), pageSize(query));
  }

  /**
   * 获取用户详情
   * @param userId 用户ID
   * @return 用户详情
   */
  @GetMapping("/users/{userId}")
  public Map<String, Object> getUserDetail(@PathVariable Long userId) {
    return buildSuccessResponse(opsService.getUserDetail(userId));
  }

  /**
   * 更新用户角色
   * @param userId 用户ID
   * @param request 包含新角色的请求体
   * @return 操作结果
   */
  @PutMapping("/users/{userId}/roles")
  public Map<String, Object> updateUserRoles(
      @PathVariable Long userId,
      @RequestBody Map<String, Object> request
  ) {
    @SuppressWarnings("unchecked")
    List<String> roles = (List<String>) request.get("roles");
    opsService.updateUserRoles(userId, roles);
    return buildSuccessResponse(Map.of("message", "角色更新成功"));
  }

  @PutMapping("/users/{userId}/status")
  public Map<String, Object> updateUserStatus(
      @PathVariable Long userId,
      @RequestBody Map<String, Object> request
  ) {
    String status = (String) request.get("status");
    opsService.updateUserStatus(userId, status);
    return buildSuccessResponse(Map.of("message", "状态更新成功"));
  }

  /**
   * 获取待审核商品列表（分页）
   * @param pageNo 页码（默认1）
   * @param pageSize 每页数量（默认10，最大100）
   * @return 待审核商品分页结果
   */
  @PostMapping("/pending-items")
  public Map<String, Object> getPendingItems(@RequestBody(required = false) OpsListRequest request) {
    OpsListRequest query = request != null ? request : new OpsListRequest(null, null, null, null, null);
    return opsService.getPendingItems(pageNo(query), pageSize(query));
  }

  /**
   * 获取待审核数量统计
   * @return 待审核数量
   */
  @GetMapping("/pending-counts")
  public Map<String, Object> getPendingCounts() {
    Map<String, Object> counts = new HashMap<>();
    counts.put("items", opsService.getPendingItemsCount());
    counts.put("reviews", reviewService.getPendingCount());
    counts.put("circle", circleService.getPendingCount());
    return buildSuccessResponse(counts);
  }

  // ========== 商品管理接口（OPS专用，不需要SELLER角色）==========

  /**
   * 商品下架（运营管理员操作）
   * @param id 商品ID
   * @return 操作结果
   */
  @PostMapping("/items/{id}/off-shelf")
  public Map<String, Object> offShelfItem(@PathVariable Long id) {
    try {
      itemService.offShelfItem(id, null); // OPS用户可以下架任何商品
      return buildSuccessResponse(Map.of("message", "商品已下架"));
    } catch (Exception e) {
      return buildErrorResponse("下架失败: " + e.getMessage());
    }
  }

  /**
   * 删除商品（运营管理员操作）
   * @param id 商品ID
   * @return 操作结果
   */
  @DeleteMapping("/items/{id}")
  public Map<String, Object> deleteItem(@PathVariable Long id) {
    try {
      itemService.deleteItem(id, null); // OPS用户可以删除任何商品
      return buildSuccessResponse(Map.of("message", "商品已删除"));
    } catch (Exception e) {
      return buildErrorResponse("删除失败: " + e.getMessage());
    }
  }

  @PostMapping("/circle/pending")
  public Map<String, Object> getPendingCirclePosts(@RequestBody(required = false) OpsListRequest request) {
    OpsListRequest query = request != null ? request : new OpsListRequest(null, null, null, null, null);
    int pageNo = pageNo(query);
    int pageSize = pageSize(query);
    return buildSuccessResponse(Map.of(
        "items", circleService.getPendingPosts(pageNo, pageSize),
        "totalCount", circleService.getPendingCount(),
        "pageNo", pageNo,
        "pageSize", pageSize));
  }

  @PostMapping("/circle/{postId}/approve")
  public Map<String, Object> approveCirclePost(@PathVariable Long postId) {
    return buildSuccessResponse(circleService.approvePost(postId));
  }

  @PostMapping("/circle/{postId}/reject")
  public Map<String, Object> rejectCirclePost(@PathVariable Long postId, @RequestBody(required = false) Map<String, Object> request) {
    String reason = request != null && request.get("reason") != null ? String.valueOf(request.get("reason")) : "运营驳回";
    return buildSuccessResponse(circleService.rejectPost(postId, reason));
  }

  private int pageNo(OpsListRequest request) {
    return request.pageNo() != null && request.pageNo() > 0 ? request.pageNo() : 1;
  }

  private int pageSize(OpsListRequest request) {
    int size = request.pageSize() != null && request.pageSize() > 0 ? request.pageSize() : 10;
    return Math.min(size, 100);
  }

  public record OpsListRequest(
      String status,
      String keyword,
      String category,
      Integer pageNo,
      Integer pageSize) {}

  /**
   * 构建成功响应
   * @param data 响应数据
   * @return 统一格式的成功响应
   */
  private Map<String, Object> buildSuccessResponse(Object data) {
    return Map.of("code", 200, "data", data);
  }

  /**
   * 构建错误响应
   * @param message 错误消息
   * @return 统一格式的错误响应
   */
  private Map<String, Object> buildErrorResponse(String message) {
    return Map.of("code", 500, "message", message);
  }
}

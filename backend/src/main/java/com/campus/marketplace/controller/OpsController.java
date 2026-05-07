package com.campus.marketplace.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.campus.marketplace.service.OpsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ops")
@PreAuthorize("isAuthenticated() and hasRole('OPS')")
public class OpsController {

  private final OpsService opsService;

  public OpsController(OpsService opsService) {
    this.opsService = opsService;
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
   * 订单分页查询
   * @param status 订单状态筛选
   * @param keyword 搜索关键词
   * @param pageNo 页码（默认1）
   * @param pageSize 每页数量（默认10，最大100）
   * @return 订单分页结果
   */
  @GetMapping("/orders")
  public Map<String, Object> getOrders(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "10") int pageSize
  ) {
    return opsService.getOrders(status, keyword, pageNo, Math.min(pageSize, 100));
  }

  /**
   * 供方列表
   * @param keyword 搜索关键词
   * @param pageNo 页码（默认1）
   * @param pageSize 每页数量（默认10，最大100）
   * @return 供方分页结果
   */
  @GetMapping("/vendors")
  public Map<String, Object> getVendors(
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "10") int pageSize
  ) {
    return opsService.getVendors(keyword, pageNo, Math.min(pageSize, 100));
  }

  /**
   * 需方列表
   * @param keyword 搜索关键词
   * @param pageNo 页码（默认1）
   * @param pageSize 每页数量（默认10，最大100）
   * @return 需方分页结果
   */
  @GetMapping("/buyers")
  public Map<String, Object> getBuyers(
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "10") int pageSize
  ) {
    return opsService.getBuyers(keyword, pageNo, Math.min(pageSize, 100));
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
  @GetMapping("/users")
  public Map<String, Object> getUsers(
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "10") int pageSize
  ) {
    return opsService.getUsers(keyword, pageNo, Math.min(pageSize, 100));
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
  @GetMapping("/pending-items")
  public Map<String, Object> getPendingItems(
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "10") int pageSize
  ) {
    return opsService.getPendingItems(pageNo, Math.min(pageSize, 100));
  }

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

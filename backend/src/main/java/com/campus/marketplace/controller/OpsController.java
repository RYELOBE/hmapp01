package com.campus.marketplace.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.campus.marketplace.service.OpsService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ops")
@SaCheckLogin
@SaCheckRole("OPS")
public class OpsController {

  private final OpsService opsService;

  public OpsController(OpsService opsService) {
    this.opsService = opsService;
  }

  /** 运营统计数据聚合 */
  @GetMapping("/statistics")
  public Map<String, Object> getStatistics() {
    return Map.of("code", 200, "data", Map.of("statistics", opsService.getStatistics()));
  }

  /** 订单分页查询 */
  @GetMapping("/orders")
  public Map<String, Object> getOrders(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "10") int pageSize
  ) {
    return opsService.getOrders(status, keyword, pageNo, pageSize);
  }

  /** 供方列表 */
  @GetMapping("/vendors")
  public Map<String, Object> getVendors(
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "10") int pageSize
  ) {
    return opsService.getVendors(keyword, pageNo, pageSize);
  }

  /** 需方列表 */
  @GetMapping("/buyers")
  public Map<String, Object> getBuyers(
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "10") int pageSize
  ) {
    return opsService.getBuyers(keyword, pageNo, pageSize);
  }
}

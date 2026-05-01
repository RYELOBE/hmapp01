package com.campus.marketplace.service;

import com.campus.marketplace.repository.ItemRepository;
import com.campus.marketplace.repository.OrderRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class OpsService {

  private final ItemRepository itemRepository;
  private final OrderRepository orderRepository;

  public OpsService(ItemRepository itemRepository, OrderRepository orderRepository) {
    this.itemRepository = itemRepository;
    this.orderRepository = orderRepository;
  }

  /** 运营统计数据聚合 */
  public Map<String, Object> getStatistics() {
    Map<String, Object> stats = new HashMap<>();
    stats.put("pendingReviews", itemRepository.countByStatus("PENDING"));
    stats.put("todayApproved", 0);   // TODO: 需要按日期筛选
    stats.put("todayRejected", 0);   // TODO: 需要按日期筛选
    stats.put("totalItems", itemRepository.countAll());
    stats.put("totalOrders", orderRepository.countAll());
    stats.put("totalVendors", itemRepository.countVendors(null));
    stats.put("totalBuyers", orderRepository.countBuyers(null));
    stats.put("activeUsers", 0);     // TODO: 需要活跃用户统计
    return stats;
  }

  /** 订单分页查询 */
  public Map<String, Object> getOrders(String status, String keyword, int pageNo, int pageSize) {
    List<Map<String, Object>> rows = orderRepository.findByPage(status, keyword, pageNo, pageSize);
    int total = orderRepository.countByFilter(status, keyword);
    return Map.of(
      "code", 200,
      "data", Map.of(
        "rows", rows,
        "totalCount", total,
        "pageNo", pageNo,
        "pageSize", pageSize
      )
    );
  }

  /** 供方列表 */
  public Map<String, Object> getVendors(String keyword, int pageNo, int pageSize) {
    List<Map<String, Object>> rows = itemRepository.findVendorsWithStats(keyword, pageNo, pageSize);
    int total = itemRepository.countVendors(keyword);
    return Map.of(
      "code", 200,
      "data", Map.of(
        "vendors", rows,
        "totalCount", total,
        "pageNo", pageNo,
        "pageSize", pageSize
      )
    );
  }

  /** 需方列表 */
  public Map<String, Object> getBuyers(String keyword, int pageNo, int pageSize) {
    List<Map<String, Object>> rows = orderRepository.findBuyersWithStats(keyword, pageNo, pageSize);
    int total = orderRepository.countBuyers(keyword);
    return Map.of(
      "code", 200,
      "data", Map.of(
        "buyers", rows,
        "totalCount", total,
        "pageNo", pageNo,
        "pageSize", pageSize
      )
    );
  }
}

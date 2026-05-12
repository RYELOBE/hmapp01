package com.campus.marketplace.service;

import com.campus.marketplace.repository.CirclePostRepository;
import com.campus.marketplace.repository.ItemRepository;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.ReviewRepository;
import com.campus.marketplace.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StatsService {

  private static final Logger logger = LoggerFactory.getLogger(StatsService.class);

  private final UserRepository userRepository;
  private final ItemRepository itemRepository;
  private final OrderRepository orderRepository;
  private final ReviewRepository reviewRepository;
  private final CirclePostRepository circlePostRepository;

  public StatsService(UserRepository userRepository, ItemRepository itemRepository,
      OrderRepository orderRepository, ReviewRepository reviewRepository,
      CirclePostRepository circlePostRepository) {
    this.userRepository = userRepository;
    this.itemRepository = itemRepository;
    this.orderRepository = orderRepository;
    this.reviewRepository = reviewRepository;
    this.circlePostRepository = circlePostRepository;
  }

  public Map<String, Object> getAllStats() {
    logger.info("获取所有统计数据");
    Map<String, Object> stats = new HashMap<>();

    stats.put("totalUsers", userRepository.countAll());
    stats.put("todayUsers", countTodayUsers());

    stats.put("totalItems", itemRepository.countAll());
    stats.put("pendingItems", itemRepository.countByStatus("PENDING_REVIEW"));
    stats.put("todayItems", itemRepository.countTodayItems());

    stats.put("totalOrders", orderRepository.countAll());
    stats.put("todayOrders", orderRepository.countTodayOrders());
    stats.put("todayCompletedOrders", orderRepository.countTodayCompletedOrders());
    stats.put("todayTotalAmount", orderRepository.sumTodayTotalAmount());
    stats.put("completedTotalAmount", orderRepository.sumCompletedTotalAmount());

    stats.put("totalReviews", reviewRepository.countAll());

    stats.put("totalPosts", circlePostRepository.countAll());
    stats.put("pendingPosts", circlePostRepository.countByStatus("PENDING"));

    return stats;
  }

  public Map<String, Object> getBriefStats() {
    logger.info("获取简要统计数据");
    Map<String, Object> stats = new HashMap<>();

    stats.put("totalUsers", userRepository.countAll());
    stats.put("todayUsers", countTodayUsers());
    stats.put("activeUsers", userRepository.countActiveUsers());

    stats.put("totalItems", itemRepository.countAll());
    stats.put("todayItems", itemRepository.countTodayItems());
    stats.put("approvedItems", itemRepository.countByStatus("APPROVED"));

    stats.put("totalOrders", orderRepository.countAll());
    stats.put("todayOrders", orderRepository.countTodayOrders());
    stats.put("todayTotalAmount", orderRepository.sumTodayTotalAmount());

    stats.put("totalPosts", circlePostRepository.countAll());
    stats.put("approvedPosts", circlePostRepository.countByStatus("APPROVED"));

    return stats;
  }

  /**
   * 获取近7天订单趋势数据
   * @return 包含7天订单数据的列表
   */
  public List<Map<String, Object>> getOrderTrend() {
    logger.info("获取订单趋势数据");
    List<Map<String, Object>> trendData = new ArrayList<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
    
    for (int i = 6; i >= 0; i--) {
      LocalDate date = LocalDate.now().minusDays(i);
      String label = date.format(formatter);
      int count = orderRepository.countByDate(date.toString());
      
      Map<String, Object> dayData = new HashMap<>();
      dayData.put("label", label);
      dayData.put("value", count);
      dayData.put("date", date.toString());
      trendData.add(dayData);
    }
    
    return trendData;
  }

  /**
   * 获取商品分类统计数据
   * @return 分类占比数据
   */
  public List<Map<String, Object>> getCategoryStats() {
    logger.info("获取分类统计数据");
    String sql = "SELECT category, COUNT(*) as count FROM item WHERE review_status = 'APPROVED' GROUP BY category ORDER BY count DESC";
    
    return itemRepository.queryForCategoryStats(sql);
  }

  /**
   * 获取订单状态分布
   * @return 各状态订单数量
   */
  public Map<String, Object> getOrderStatusDistribution() {
    logger.info("获取订单状态分布");
    Map<String, Object> distribution = new HashMap<>();
    
    distribution.put("pendingPayment", orderRepository.countByStatus("PENDING_PAYMENT"));
    distribution.put("paid", orderRepository.countByStatus("PAID"));
    distribution.put("shipped", orderRepository.countByStatus("SHIPPED"));
    distribution.put("completed", orderRepository.countByStatus("COMPLETED"));
    distribution.put("cancelled", orderRepository.countByStatus("CANCELLED"));
    
    return distribution;
  }

  /**
   * 获取最近活动记录
   * @param limit 返回数量限制
   * @return 最近活动列表
   */
  public List<Map<String, Object>> getRecentActivities(int limit) {
    logger.info("获取最近活动记录");
    List<Map<String, Object>> activities = new ArrayList<>();
    
    // 获取最近注册用户
    List<Map<String, Object>> recentUsers = userRepository.findRecentUsers(Math.min(limit, 5));
    for (Map<String, Object> user : recentUsers) {
      Map<String, Object> activity = new HashMap<>();
      activity.put("id", "user_" + user.get("id"));
      activity.put("type", "user");
      activity.put("title", "新用户注册：" + user.get("username"));
      activity.put("color", "#165DFF");
      activity.put("time", user.get("createdAt"));
      activities.add(activity);
    }
    
    // 获取最近发布的商品
    List<Map<String, Object>> recentItems = itemRepository.findRecentItems(Math.min(limit, 5));
    for (Map<String, Object> item : recentItems) {
      Map<String, Object> activity = new HashMap<>();
      activity.put("id", "item_" + item.get("id"));
      activity.put("type", "item");
      activity.put("title", "发布新商品：" + item.get("title"));
      activity.put("color", "#00B42A");
      activity.put("time", item.get("createdAt"));
      activities.add(activity);
    }
    
    // 获取最近完成的订单
    List<Map<String, Object>> recentOrders = orderRepository.findRecentOrders(Math.min(limit, 3), "COMPLETED");
    for (Map<String, Object> order : recentOrders) {
      Map<String, Object> activity = new HashMap<>();
      activity.put("id", "order_" + order.get("id"));
      activity.put("type", "order");
      String buyerName = order.get("buyerName") != null ? order.get("buyerName").toString() : "买家";
      Double amount = order.get("totalAmount") != null ? ((Number) order.get("totalAmount")).doubleValue() : 0.0;
      activity.put("title", String.format("完成订单：%s ¥%.2f", buyerName, amount));
      activity.put("color", "#FF7D00");
      activity.put("time", order.get("createdAt"));
      activities.add(activity);
    }
    
    // 按时间排序并限制数量
    activities.sort((a, b) -> {
      String timeA = a.get("time") != null ? a.get("time").toString() : "";
      String timeB = b.get("time") != null ? b.get("time").toString() : "";
      return timeB.compareTo(timeA);
    });
    
    return activities.stream().limit(limit).toList();
  }

  private long countTodayUsers() {
    return userRepository.countTodayNewUsers();
  }
}

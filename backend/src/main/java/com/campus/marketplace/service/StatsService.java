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
import java.util.HashMap;
import java.util.Map;

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

    stats.put("totalReviews", reviewRepository.countByItemId(0L) + reviewRepository.countByItemId(-1L));

    stats.put("totalPosts", circlePostRepository.countAll());
    stats.put("pendingPosts", circlePostRepository.countByStatus("PENDING"));

    return stats;
  }

  public Map<String, Object> getBriefStats() {
    logger.info("获取简要统计数据");
    Map<String, Object> stats = new HashMap<>();

    stats.put("totalUsers", userRepository.countAll());
    stats.put("activeUsers", userRepository.countActiveUsers());

    stats.put("totalItems", itemRepository.countAll());
    stats.put("approvedItems", itemRepository.countByStatus("APPROVED"));

    stats.put("totalOrders", orderRepository.countAll());
    stats.put("todayOrders", orderRepository.countTodayOrders());

    stats.put("totalPosts", circlePostRepository.countAll());
    stats.put("approvedPosts", circlePostRepository.countByStatus("APPROVED"));

    return stats;
  }

  private long countTodayUsers() {
    LocalDateTime today = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = today.plusDays(1);
    return userRepository.countByDateRange(today, endOfDay);
  }
}

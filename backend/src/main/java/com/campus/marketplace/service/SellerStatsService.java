package com.campus.marketplace.service;

import com.campus.marketplace.repository.ItemRepository;
import com.campus.marketplace.repository.OrderRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class SellerStatsService {

  private final OrderRepository orderRepository;
  private final ItemRepository itemRepository;

  public SellerStatsService(OrderRepository orderRepository, ItemRepository itemRepository) {
    this.orderRepository = orderRepository;
    this.itemRepository = itemRepository;
  }

  public Map<String, Object> getOverview(Long sellerId) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
    LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();

    int todaySales = orderRepository.sumTotalAmountBySellerAndStatusAndTimeRange(sellerId, "COMPLETED", startOfToday, now);
    int todayOrders = orderRepository.countBySellerAndStatusAndTimeRange(sellerId, "COMPLETED", startOfToday, now);

    int monthSales = orderRepository.sumTotalAmountBySellerAndStatusAndTimeRange(sellerId, "COMPLETED", startOfMonth, now);
    int monthOrders = orderRepository.countBySellerAndStatusAndTimeRange(sellerId, "COMPLETED", startOfMonth, now);

    int totalSales = orderRepository.sumTotalAmountBySellerAndStatus(sellerId, "COMPLETED");
    int totalOrders = orderRepository.countBySellerAndStatus(sellerId, "COMPLETED");

    int onSaleCount = itemRepository.countBySellerIdAndStatus(sellerId, "APPROVED");
    int pendingCount = itemRepository.countBySellerIdAndStatus(sellerId, "PENDING_REVIEW");

    Map<String, Object> stats = new HashMap<>();
    stats.put("todaySales", todaySales);
    stats.put("todayOrders", todayOrders);
    stats.put("monthSales", monthSales);
    stats.put("monthOrders", monthOrders);
    stats.put("totalSales", totalSales);
    stats.put("totalOrders", totalOrders);
    stats.put("onSaleCount", onSaleCount);
    stats.put("pendingCount", pendingCount);

    return Map.of("code", 200, "data", stats);
  }

  public Map<String, Object> getTrend(Long sellerId, int days) {
    if (days != 7 && days != 30) {
      days = 7;
    }

    LocalDateTime now = LocalDateTime.now();
    List<Map<String, Object>> trendData = new ArrayList<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

    for (int i = days - 1; i >= 0; i--) {
      LocalDate date = now.minusDays(i).toLocalDate();
      LocalDateTime startOfDay = date.atStartOfDay();
      LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

      int sales = orderRepository.sumTotalAmountBySellerAndStatusAndTimeRange(sellerId, "COMPLETED", startOfDay, endOfDay);
      int orders = orderRepository.countBySellerAndStatusAndTimeRange(sellerId, "COMPLETED", startOfDay, endOfDay);

      Map<String, Object> dayData = new HashMap<>();
      dayData.put("date", date.format(formatter));
      dayData.put("sales", sales);
      dayData.put("orders", orders);
      trendData.add(dayData);
    }

    return Map.of("code", 200, "data", trendData);
  }

  public Map<String, Object> getRanking(Long sellerId, int limit) {
    List<Map<String, Object>> items = itemRepository.findBySellerId(sellerId);
    List<Map<String, Object>> rankedItems = new ArrayList<>();

    for (Map<String, Object> item : items) {
      Long itemId = (Long) item.get("id");
      int soldCount = orderRepository.countByItemIdAndSellerIdAndStatus(itemId, sellerId, "COMPLETED");

      Map<String, Object> rankedItem = new HashMap<>(item);
      rankedItem.put("soldCount", soldCount);
      rankedItems.add(rankedItem);
    }

    rankedItems.sort((a, b) -> {
      int soldA = (int) a.getOrDefault("soldCount", 0);
      int soldB = (int) b.getOrDefault("soldCount", 0);
      return Integer.compare(soldB, soldA);
    });

    if (rankedItems.size() > limit) {
      rankedItems = rankedItems.subList(0, limit);
    }

    return Map.of("code", 200, "data", rankedItems);
  }
}

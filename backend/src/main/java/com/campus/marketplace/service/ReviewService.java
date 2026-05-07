package com.campus.marketplace.service;

import com.campus.marketplace.repository.ItemRepository;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.ReviewRepository;
import com.campus.marketplace.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final OrderRepository orderRepository;
  private final UserRepository userRepository;
  private final ItemRepository itemRepository;

  public ReviewService(ReviewRepository reviewRepository, OrderRepository orderRepository,
      UserRepository userRepository, ItemRepository itemRepository) {
    this.reviewRepository = reviewRepository;
    this.orderRepository = orderRepository;
    this.userRepository = userRepository;
    this.itemRepository = itemRepository;
  }

  public Map<String, Object> createReview(Long buyerId, Long orderId, Long itemId, int rating, String content, String images) {
    if (rating < 1 || rating > 5) {
      throw new IllegalArgumentException("评分必须在1-5之间");
    }

    var order = orderRepository.findById(orderId);
    if (order == null) {
      throw new IllegalArgumentException("订单不存在");
    }
    if (!buyerId.equals(((Number) order.get("buyerId")).longValue())) {
      throw new IllegalArgumentException("只能评价自己的订单");
    }

    if (!"COMPLETED".equals(order.get("status"))) {
      throw new IllegalArgumentException("仅可评价已完成的订单");
    }

    if (reviewRepository.existsByOrderId(orderId)) {
      throw new IllegalArgumentException("该订单已评价");
    }

    Long sellerId = ((Number) order.get("sellerId")).longValue();
    Map<String, Object> review = reviewRepository.create(orderId, itemId, buyerId, sellerId, rating, content, images);

    Map<String, Object> enrichedReview = new java.util.HashMap<>(review);
    var userOpt = userRepository.findById(buyerId);
    userOpt.ifPresent(user -> enrichedReview.put("buyerNickname", user.get("nickname")));

    return Map.of("code", 200, "message", "评价成功", "data", enrichedReview);
  }

  public Map<String, Object> getReviewsByItem(Long itemId, int page, int pageSize) {
    List<Map<String, Object>> reviews = reviewRepository.findByItemIdAndStatus(itemId, "APPROVED");
    int total = reviews.size();
    int start = (page - 1) * pageSize;
    int end = Math.min(start + pageSize, total);

    List<Map<String, Object>> pagedReviews = new ArrayList<>();
    for (int i = start; i < end; i++) {
      Map<String, Object> review = reviews.get(i);
      Map<String, Object> enriched = new java.util.HashMap<>(review);
      Long buyerId = (Long) review.get("buyerId");
      userRepository.findById(buyerId).ifPresent(user -> enriched.put("buyerNickname", user.get("nickname")));
      pagedReviews.add(enriched);
    }

    int averageRating = 0;
    if (total > 0) {
      Double avg = reviewRepository.averageRatingByItemId(itemId);
      averageRating = avg != null ? (int) Math.round(avg) : 0;
    }

    return Map.of("code", 200, "data", pagedReviews, "total", total, "averageRating", averageRating,
        "ratingDistribution", reviewRepository.getRatingDistribution(itemId));
  }

  public Map<String, Object> getMyReviews(Long buyerId, int page, int pageSize) {
    List<Map<String, Object>> allReviews = reviewRepository.findByBuyerId(buyerId);
    int total = allReviews.size();
    int start = (page - 1) * pageSize;
    int end = Math.min(start + pageSize, total);

    List<Map<String, Object>> pagedReviews = new ArrayList<>();
    for (int i = start; i < end; i++) {
      Map<String, Object> review = allReviews.get(i);
      Map<String, Object> enriched = new java.util.HashMap<>(review);
      Long itemId = (Long) review.get("itemId");
      Map<String, Object> item = itemRepository.findById(itemId);
      if (item != null) {
        enriched.put("itemTitle", item.get("title"));
        enriched.put("itemImage", item.get("imageUrls"));
      }
      pagedReviews.add(enriched);
    }

    return Map.of("code", 200, "data", pagedReviews, "total", total);
  }

  public Map<String, Object> getPendingReviews(int page, int pageSize) {
    List<Map<String, Object>> allReviews = reviewRepository.findByStatus("PENDING");
    int total = allReviews.size();
    int start = (page - 1) * pageSize;
    int end = Math.min(start + pageSize, total);

    List<Map<String, Object>> pagedReviews = new ArrayList<>(allReviews.subList(start, end));

    for (Map<String, Object> review : pagedReviews) {
      Long buyerId = (Long) review.get("buyerId");
      userRepository.findById(buyerId).ifPresent(user -> review.put("buyerNickname", user.get("nickname")));
      Long itemId = (Long) review.get("itemId");
      Map<String, Object> item = itemRepository.findById(itemId);
      if (item != null) {
        review.put("itemTitle", item.get("title"));
      }
    }

    return Map.of("code", 200, "data", pagedReviews, "total", total);
  }

  public Map<String, Object> approveReview(Long reviewId) {
    var reviewOpt = reviewRepository.findById(reviewId);
    if (reviewOpt.isEmpty()) {
      throw new IllegalArgumentException("评价不存在");
    }
    reviewRepository.updateStatus(reviewId, "APPROVED", null);
    return Map.of("code", 200, "message", "审核通过", "data", reviewRepository.findById(reviewId).get());
  }

  public Map<String, Object> rejectReview(Long reviewId, String reason) {
    var reviewOpt = reviewRepository.findById(reviewId);
    if (reviewOpt.isEmpty()) {
      throw new IllegalArgumentException("评价不存在");
    }
    String replyContent = "拒绝原因：" + reason;
    reviewRepository.updateStatus(reviewId, "REJECTED", replyContent);
    return Map.of("code", 200, "message", "已驳回", "data", reviewRepository.findById(reviewId).get());
  }

  public Map<String, Object> getReviewByOrder(Long buyerId, Long orderId) {
    var order = orderRepository.findById(orderId);
    if (order == null) {
      throw new IllegalArgumentException("订单不存在");
    }
    if (((Number)order.get("buyerId")).longValue() != buyerId) {
      throw new IllegalArgumentException("无权查看此订单评价");
    }

    var reviewOpt = reviewRepository.findByOrderId(orderId);
    if (reviewOpt.isEmpty()) {
      return Map.of("code", 404, "message", "该订单暂无评价");
    }
    return Map.of("code", 200, "data", reviewOpt.get());
  }

  public Map<String, Object> replyToReview(Long userId, Long reviewId, String content) {
    var reviewOpt = reviewRepository.findById(reviewId);
    if (reviewOpt.isEmpty()) {
      throw new IllegalArgumentException("评价不存在");
    }
    Map<String, Object> review = reviewOpt.get();
    Long sellerId = review.get("sellerId") != null ? ((Number) review.get("sellerId")).longValue() : null;

    if (sellerId == null || !sellerId.equals(userId)) {
      throw new IllegalArgumentException("无权回复此评价");
    }

    reviewRepository.updateReply(reviewId, content);
    return Map.of("code", 200, "message", "回复成功");
  }

  public Map<String, Object> getReviewStats(Long itemId) {
    int total = reviewRepository.countByItemId(itemId);
    Double avg = reviewRepository.averageRatingByItemId(itemId);
    int averageRating = avg != null ? (int) Math.round(avg) : 0;

    return Map.of("code", 200, "data", Map.of(
        "total", total,
        "averageRating", averageRating,
        "ratingDistribution", reviewRepository.getRatingDistribution(itemId)
    ));
  }

  public long getPendingCount() {
    return reviewRepository.countByStatus("PENDING");
  }

  public Map<String, Object> getReviewQueuePaged(String status, int pageNo, int pageSize) {
    List<Map<String, Object>> rows = itemRepository.findByPage(status, null, null, pageNo, pageSize);
    int total = itemRepository.countByFilter(status, null, null);
    return Map.of("code", 200, "data", Map.of("items", rows, "totalCount", total, "pageNo", pageNo, "pageSize", pageSize));
  }

  public Map<String, Object> approve(Long itemId, Long operatorId) {
    itemRepository.updateReviewStatus(itemId, "APPROVED", "审核通过");
    return Map.of("code", 200, "message", "审核已通过");
  }

  public Map<String, Object> reject(Long itemId, Long operatorId, String reason) {
    itemRepository.updateReviewStatus(itemId, "REJECTED", reason);
    return Map.of("code", 200, "message", "已驳回");
  }
}

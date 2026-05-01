package com.campus.marketplace.service;

import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.ReviewRepository;
import com.campus.marketplace.repository.UserAccountRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final OrderRepository orderRepository;
  private final UserAccountRepository userAccountRepository;

  public ReviewService(ReviewRepository reviewRepository, OrderRepository orderRepository,
      UserAccountRepository userAccountRepository) {
    this.reviewRepository = reviewRepository;
    this.orderRepository = orderRepository;
    this.userAccountRepository = userAccountRepository;
  }

  public Map<String, Object> createReview(Long buyerId, Long orderId, Long itemId, int rating, String content, String images) {
    if (rating < 1 || rating > 5) {
      throw new IllegalArgumentException("评分必须在1-5之间");
    }

    var orderOpt = orderRepository.findById(orderId);
    if (orderOpt.isEmpty()) {
      throw new IllegalArgumentException("订单不存在");
    }
    Map<String, Object> order = orderOpt.get();

    if (!"COMPLETED".equals(order.get("status"))) {
      throw new IllegalArgumentException("仅可评价已完成的订单");
    }

    if (reviewRepository.existsByOrderId(orderId)) {
      throw new IllegalArgumentException("该订单已评价");
    }

    Map<String, Object> review = reviewRepository.create(orderId, itemId, buyerId, rating, content, images);

    Map<String, Object> enrichedReview = new java.util.HashMap<>(review);
    var userOpt = userAccountRepository.findById(buyerId);
    userOpt.ifPresent(user -> enrichedReview.put("buyerNickname", user.get("nickname")));

    return Map.of("code", 200, "message", "评价成功", "data", enrichedReview);
  }

  public Map<String, Object> getReviewsByItem(Long itemId, int page, int pageSize) {
    List<Map<String, Object>> reviews = reviewRepository.findByItemId(itemId);
    int total = reviews.size();
    int start = (page - 1) * pageSize;
    int end = Math.min(start + pageSize, total);

    List<Map<String, Object>> pagedReviews = new ArrayList<>();
    for (int i = start; i < end; i++) {
      Map<String, Object> review = reviews.get(i);
      Map<String, Object> enriched = new java.util.HashMap<>(review);
      Long buyerId = (Long) review.get("buyerId");
      userAccountRepository.findById(buyerId).ifPresent(user -> enriched.put("buyerNickname", user.get("nickname")));
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

  public Map<String, Object> getReviewByOrder(Long buyerId, Long orderId) {
    var orderOpt = orderRepository.findById(orderId);
    if (orderOpt.isEmpty()) {
      throw new IllegalArgumentException("订单不存在");
    }
    Map<String, Object> order = orderOpt.get();
    if (!buyerId.equals(order.get("buyerId"))) {
      throw new IllegalArgumentException("无权查看此订单评价");
    }

    var reviewOpt = reviewRepository.findByOrderId(orderId);
    if (reviewOpt.isEmpty()) {
      return Map.of("code", 404, "message", "该订单暂无评价");
    }
    return Map.of("code", 200, "data", reviewOpt.get());
  }

  public Map<String, Object> replyToReview(Long sellerId, Long reviewId, String content) {
    var reviewOpt = reviewRepository.findById(reviewId);
    if (reviewOpt.isEmpty()) {
      throw new IllegalArgumentException("评价不存在");
    }
    Map<String, Object> review = reviewOpt.get();

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
}

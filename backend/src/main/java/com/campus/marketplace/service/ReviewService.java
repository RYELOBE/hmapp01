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

    Long sellerId;

    if (orderId != null) {
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
      var existing = reviewRepository.findByOrderId(orderId);
      if (existing.isPresent()) {
        throw new IllegalArgumentException("该订单已评价");
      }
      sellerId = ((Number) order.get("sellerId")).longValue();
    } else {
      var item = itemRepository.findById(itemId);
      if (item == null) {
        throw new IllegalArgumentException("商品不存在");
      }
      sellerId = ((Number) item.get("sellerId")).longValue();
    }

    Map<String, Object> review = reviewRepository.createWithNullableOrder(orderId, itemId, buyerId, sellerId, rating, content, images);

    Map<String, Object> enrichedReview = new java.util.HashMap<>(review);
    var userOpt = userRepository.findById(buyerId);
    userOpt.ifPresent(user -> {
      enrichedReview.put("buyerNickname", user.get("nickname"));
      enrichedReview.put("userName", user.get("nickname"));
    });

    return Map.of("code", 200, "message", "评价成功", "data", enrichedReview);
  }

  public Map<String, Object> getReviewsByItem(Long itemId, int page, int pageSize) {
    List<Map<String, Object>> reviews = reviewRepository.findByItemIdPaged(itemId, page, pageSize);
    int total = reviewRepository.countByItemId(itemId);

    List<Map<String, Object>> enrichedReviews = enrichReviews(reviews);

    int averageRating = 0;
    if (total > 0) {
      Double avg = reviewRepository.averageRatingByItemId(itemId);
      averageRating = avg != null ? (int) Math.round(avg) : 0;
    }

    return Map.of("code", 200, "data", Map.of(
        "items", enrichedReviews,
        "totalCount", total,
        "averageRating", averageRating,
        "ratingDistribution", reviewRepository.getRatingDistribution(itemId)
    ));
  }

  /** POST方式查询评价列表（支持状态筛选） */
  public Map<String, Object> queryReviewsByItem(Long itemId, int page, int pageSize, String status) {
    List<Map<String, Object>> reviews;

    if (status != null && !status.isEmpty()) {
      reviews = reviewRepository.findByItemIdAndStatusPaged(itemId, status, page, pageSize);
    } else {
      reviews = reviewRepository.findByItemIdPaged(itemId, page, pageSize);
    }

    int total = status != null && !status.isEmpty()
        ? reviewRepository.countByItemIdAndStatus(itemId, status)
        : reviewRepository.countByItemId(itemId);

    List<Map<String, Object>> enrichedReviews = enrichReviews(reviews);

    return Map.of(
        "code", 200,
        "data", Map.of(
            "items", enrichedReviews,
            "list", enrichedReviews,
            "records", enrichedReviews,
            "total", total,
            "totalCount", total,
            "pageNo", page,
            "pageSize", pageSize
        )
    );
  }

  private List<Map<String, Object>> enrichReviews(List<Map<String, Object>> reviews) {
    List<Map<String, Object>> enrichedReviews = new ArrayList<>();
    for (Map<String, Object> review : reviews) {
      Map<String, Object> enriched = new java.util.HashMap<>(review);
      Long buyerId = (Long) review.get("buyerId");
      userRepository.findById(buyerId).ifPresent(user -> {
        enriched.put("userName", user.get("nickname"));
        enriched.put("buyerNickname", user.get("nickname"));
      });
      enrichedReviews.add(enriched);
    }
    return enrichedReviews;
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
    return Map.of("code", 200, "data", List.of(), "total", 0);
  }

  public Map<String, Object> approveReview(Long reviewId) {
    return Map.of("code", 200, "message", "审核功能暂不可用");
  }

  public Map<String, Object> rejectReview(Long reviewId, String reason) {
    return Map.of("code", 200, "message", "审核功能暂不可用");
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
    return 0;
  }

  public Map<String, Object> getReviewQueuePaged(String status, int pageNo, int pageSize) {
    return getReviewQueuePaged(status, null, null, pageNo, pageSize);
  }

  public Map<String, Object> getReviewQueuePaged(String status, String keyword, String category, int pageNo, int pageSize) {
    List<Map<String, Object>> rows = itemRepository.findByPage(status, keyword, category, pageNo, pageSize);
    int total = itemRepository.countByFilter(status, keyword, category);
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

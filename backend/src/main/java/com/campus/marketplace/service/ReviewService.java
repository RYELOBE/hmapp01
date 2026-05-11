package com.campus.marketplace.service;

import com.campus.marketplace.repository.ItemRepository;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.ReviewRepository;
import com.campus.marketplace.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
  private final NotificationService notificationService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public ReviewService(ReviewRepository reviewRepository, OrderRepository orderRepository,
      UserRepository userRepository, ItemRepository itemRepository,
      NotificationService notificationService) {
    this.reviewRepository = reviewRepository;
    this.orderRepository = orderRepository;
    this.userRepository = userRepository;
    this.itemRepository = itemRepository;
    this.notificationService = notificationService;
  }

  public Map<String, Object> createReview(Long buyerId, Long orderId, Long itemId, int rating, String content, Object images) {
    if (rating < 1 || rating > 5) {
      throw new IllegalArgumentException("评分必须在1-5之间");
    }

    String normalizedContent = content == null ? "" : content.trim();
    String normalizedImages = normalizeImages(images);
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
      Long orderItemId = ((Number) order.get("itemId")).longValue();
      if (!orderItemId.equals(itemId)) {
        throw new IllegalArgumentException("订单商品信息不匹配");
      }
      var existing = reviewRepository.findByOrderId(orderId);
      if (existing.isPresent()) {
        throw new IllegalArgumentException("该订单已评价");
      }
      sellerId = ((Number) order.get("sellerId")).longValue();
    } else {
      // 无orderId时，允许任何人评价商品
      var item = itemRepository.findById(itemId);
      if (item == null) {
        throw new IllegalArgumentException("商品不存在");
      }
      sellerId = ((Number) item.get("sellerId")).longValue();
    }

    Map<String, Object> review = reviewRepository.createWithNullableOrder(
        orderId, itemId, buyerId, sellerId, rating, normalizedContent, normalizedImages);

    Map<String, Object> enrichedReview = new java.util.HashMap<>(review);
    var userOpt = userRepository.findById(buyerId);
    userOpt.ifPresent(user -> {
      enrichedReview.put("buyerNickname", user.get("nickname"));
      enrichedReview.put("buyerName", user.get("nickname"));
      enrichedReview.put("userName", user.get("nickname"));
    });

    // 通知卖家有新评价
    Long reviewId = ((Number) review.get("id")).longValue();
    var item = itemRepository.findById(itemId);
    String itemTitle = item != null ? (String) item.get("title") : "商品";
    String buyerName = userOpt.map(u -> (String) u.get("nickname")).orElse("买家");
    notificationService.sendNotification(
        sellerId, "收到新评价",
        String.format("%s 对您的商品《%s》发表了评价，评分：%d星", buyerName, itemTitle, rating),
        "REVIEW", String.valueOf(reviewId), "REVIEW");

    return Map.of("code", 200, "message", "评价已提交，待审核后展示", "data", enrichedReview);
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
      Object buyerIdObj = review.get("buyerId");
      if (buyerIdObj instanceof Number buyerIdNumber) {
        Long buyerId = buyerIdNumber.longValue();
        userRepository.findById(buyerId).ifPresent(user -> {
          enriched.put("userName", user.get("nickname"));
          enriched.put("buyerNickname", user.get("nickname"));
          enriched.put("buyerName", user.get("nickname"));
        });
      }
      enrichedReviews.add(enriched);
    }
    return enrichedReviews;
  }

  public Map<String, Object> getMyReviews(Long buyerId, int page, int pageSize, String status) {
    List<Map<String, Object>> allReviews = reviewRepository.findByBuyerId(buyerId);
    
    // 按状态筛选
    if (status != null && !status.isEmpty()) {
      allReviews = allReviews.stream()
          .filter(review -> status.equals(review.get("status")))
          .toList();
    }
    
    int total = allReviews.size();
    int start = (page - 1) * pageSize;
    int end = Math.min(start + pageSize, total);

    List<Map<String, Object>> pagedReviews = new ArrayList<>();
    for (int i = start; i < end; i++) {
      Map<String, Object> review = allReviews.get(i);
      Map<String, Object> enriched = new java.util.HashMap<>(review);
      Long itemId = ((Number) review.get("itemId")).longValue();
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
    List<Map<String, Object>> reviews = reviewRepository.findPendingPaged(page, pageSize);
    int total = reviewRepository.countPending();
    List<Map<String, Object>> enrichedReviews = enrichPendingReviews(reviews);
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

  /** 获取所有评价列表（支持状态筛选） */
  public Map<String, Object> getAllReviews(int page, int pageSize, String status) {
    List<Map<String, Object>> reviews;
    int total;
    
    if (status != null && !status.isEmpty()) {
      reviews = reviewRepository.findByStatusPaged(status, page, pageSize);
      total = reviewRepository.countByStatus(status);
    } else {
      reviews = reviewRepository.findAllPaged(page, pageSize);
      total = reviewRepository.countAll();
    }
    
    List<Map<String, Object>> enrichedReviews = enrichPendingReviews(reviews);
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

  public Map<String, Object> approveReview(Long reviewId) {
    Map<String, Object> review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("评价不存在"));
    String currentStatus = String.valueOf(review.getOrDefault("status", ""));
    if (!"PENDING".equals(currentStatus)) {
      throw new IllegalArgumentException("只能审核待审核状态的评价");
    }
    reviewRepository.updateStatus(reviewId, "APPROVED");
    
    // 通知买家审核通过
    Long buyerId = ((Number) review.get("buyerId")).longValue();
    Long itemId = ((Number) review.get("itemId")).longValue();
    var item = itemRepository.findById(itemId);
    String itemTitle = item != null ? (String) item.get("title") : "商品";
    notificationService.sendNotification(
        buyerId, "评价审核通过",
        String.format("您对《%s》的评价已审核通过", itemTitle),
        "REVIEW", String.valueOf(reviewId), "REVIEW");
    
    return Map.of("code", 200, "message", "评价审核通过");
  }

  public Map<String, Object> rejectReview(Long reviewId, String reason) {
    Map<String, Object> review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("评价不存在"));
    String currentStatus = String.valueOf(review.getOrDefault("status", ""));
    if (!"PENDING".equals(currentStatus)) {
      throw new IllegalArgumentException("只能驳回待审核状态的评价");
    }
    reviewRepository.updateStatus(reviewId, "REJECTED");
    
    // 通知买家审核未通过
    Long buyerId = ((Number) review.get("buyerId")).longValue();
    Long itemId = ((Number) review.get("itemId")).longValue();
    var item = itemRepository.findById(itemId);
    String itemTitle = item != null ? (String) item.get("title") : "商品";
    notificationService.sendNotification(
        buyerId, "评价审核未通过",
        String.format("您对《%s》的评价未通过审核，原因：%s", itemTitle, reason != null ? reason : "无"),
        "REVIEW", String.valueOf(reviewId), "REVIEW");
    
    return Map.of("code", 200, "message", "评价已驳回", "data", Map.of("reason", reason));
  }

  public void deleteReview(Long reviewId) {
    reviewRepository.findById(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("评价不存在"));
    reviewRepository.deleteById(reviewId);
  }

  public Map<String, Object> getReviewById(Long reviewId) {
    Map<String, Object> review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("评价不存在"));
    Map<String, Object> enriched = new java.util.HashMap<>(review);
    Object buyerIdObj = review.get("buyerId");
    if (buyerIdObj instanceof Number buyerIdNumber) {
      Long buyerId = buyerIdNumber.longValue();
      userRepository.findById(buyerId).ifPresent(user -> {
        enriched.put("userName", user.get("nickname"));
        enriched.put("buyerNickname", user.get("nickname"));
        enriched.put("buyerName", user.get("nickname"));
      });
    }
    return Map.of("code", 200, "data", enriched);
  }

  public Map<String, Object> getReviewByOrder(Long buyerId, Long orderId) {
    var order = orderRepository.findById(orderId);
    if (order == null) {
      throw new IllegalArgumentException("订单不存在");
    }
    if (((Number) order.get("buyerId")).longValue() != buyerId) {
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
    
    // 通知买家有新回复
    Map<String, Object> review = reviewOpt.get();
    Long buyerId = ((Number) review.get("buyerId")).longValue();
    Long itemId = ((Number) review.get("itemId")).longValue();
    var item = itemRepository.findById(itemId);
    String itemTitle = item != null ? (String) item.get("title") : "商品";
    notificationService.sendNotification(
        buyerId, "评价已回复",
        String.format("卖家回复了您对《%s》的评价：%s", itemTitle, content),
        "REVIEW", String.valueOf(reviewId), "REVIEW");
    
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
    return reviewRepository.countPending();
  }

  public Map<String, Object> getReviewQueuePaged(String status, int pageNo, int pageSize) {
    return getReviewQueuePaged(status, null, null, pageNo, pageSize);
  }

  public Map<String, Object> getReviewQueuePaged(String status, String keyword, String category, int pageNo, int pageSize) {
    List<Map<String, Object>> reviews;
    int total;

    if (keyword != null && !keyword.isEmpty()) {
      reviews = reviewRepository.findByKeywordPaged(keyword, status, pageNo, pageSize);
      total = reviewRepository.countByKeywordAndStatus(keyword, status);
    } else if (status != null && !status.isEmpty()) {
      reviews = reviewRepository.findByStatusPaged(status, pageNo, pageSize);
      total = reviewRepository.countByStatus(status);
    } else {
      reviews = reviewRepository.findAllPaged(pageNo, pageSize);
      total = reviewRepository.countAll();
    }

    List<Map<String, Object>> enrichedReviews = enrichReviewsWithTarget(reviews);

    return Map.of(
        "code", 200,
        "data", Map.of(
            "reviews", enrichedReviews,
            "items", enrichedReviews,
            "rows", enrichedReviews,
            "totalCount", total,
            "total", total,
            "pageNo", pageNo,
            "pageSize", pageSize
        )
    );
  }

  public Map<String, Object> approve(Long reviewId, Long operatorId) {
    reviewRepository.updateStatus(reviewId, "APPROVED");
    return Map.of("code", 200, "message", "审核已通过");
  }

  public Map<String, Object> reject(Long reviewId, Long operatorId, String reason) {
    reviewRepository.updateStatus(reviewId, "REJECTED");
    return Map.of("code", 200, "message", "已驳回");
  }

  private List<Map<String, Object>> enrichPendingReviews(List<Map<String, Object>> reviews) {
    List<Map<String, Object>> enrichedReviews = new ArrayList<>();
    for (Map<String, Object> review : reviews) {
      Map<String, Object> enriched = new java.util.HashMap<>(review);
      Object buyerIdObj = review.get("buyerId");
      if (buyerIdObj instanceof Number buyerIdNumber) {
        Long buyerId = buyerIdNumber.longValue();
        userRepository.findById(buyerId).ifPresent(user -> {
          enriched.put("buyerName", user.get("nickname"));
          enriched.put("buyerNickname", user.get("nickname"));
          enriched.put("userName", user.get("nickname"));
        });
      }

      Object itemIdObj = review.get("itemId");
      if (itemIdObj instanceof Number itemIdNumber) {
        Map<String, Object> item = itemRepository.findById(itemIdNumber.longValue());
        if (item != null) {
          enriched.put("itemTitle", item.get("title"));
          enriched.put("itemImage", extractFirstImage(item.get("imageUrls")));
        }
      }

      enrichedReviews.add(enriched);
    }
    return enrichedReviews;
  }

  private String extractFirstImage(Object imageValue) {
    if (imageValue == null) {
      return "";
    }

    if (imageValue instanceof String raw) {
      String trimmed = raw.trim();
      if (trimmed.isEmpty()) {
        return "";
      }
      if (trimmed.startsWith("[")) {
        try {
          List<?> urls = objectMapper.readValue(trimmed, List.class);
          if (!urls.isEmpty() && urls.get(0) != null) {
            return String.valueOf(urls.get(0));
          }
          return "";
        } catch (Exception ignored) {
          return trimmed;
        }
      }
      return trimmed;
    }

    if (imageValue instanceof List<?> urls && !urls.isEmpty() && urls.get(0) != null) {
      return String.valueOf(urls.get(0));
    }

    return String.valueOf(imageValue);
  }

  private List<Map<String, Object>> enrichReviewsWithTarget(List<Map<String, Object>> reviews) {
    List<Map<String, Object>> enrichedReviews = new ArrayList<>();
    for (Map<String, Object> review : reviews) {
      Map<String, Object> enriched = new java.util.HashMap<>(review);

      Object buyerIdObj = review.get("buyerId");
      if (buyerIdObj instanceof Number buyerIdNumber) {
        Long buyerId = buyerIdNumber.longValue();
        userRepository.findById(buyerId).ifPresent(user -> {
          enriched.put("buyerName", user.get("nickname"));
          enriched.put("buyerNickname", user.get("nickname"));
          enriched.put("userName", user.get("nickname"));
        });
      }

      Object itemIdObj = review.get("itemId");
      if (itemIdObj instanceof Number itemIdNumber) {
        Map<String, Object> item = itemRepository.findById(itemIdNumber.longValue());
        if (item != null) {
          enriched.put("targetTitle", item.get("title"));
          enriched.put("itemTitle", item.get("title"));
          enriched.put("itemImage", extractFirstImage(item.get("imageUrls")));
          enriched.put("type", "ITEM");
        }
      }

      enrichedReviews.add(enriched);
    }
    return enrichedReviews;
  }

  private String normalizeImages(Object images) {
    if (images == null) {
      return null;
    }

    try {
      if (images instanceof String raw) {
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
          return null;
        }
        if (trimmed.startsWith("[")) {
          return trimmed;
        }
        return objectMapper.writeValueAsString(List.of(trimmed));
      }
      return objectMapper.writeValueAsString(images);
    } catch (Exception e) {
      throw new IllegalArgumentException("图片数据格式错误");
    }
  }
}

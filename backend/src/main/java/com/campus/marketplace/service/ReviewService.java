package com.campus.marketplace.service;

import com.campus.marketplace.repository.ItemRepository;
import com.campus.marketplace.repository.ReviewRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {
  private final ReviewRepository reviewRepository;
  private final ItemRepository itemRepository;

  public ReviewService(ReviewRepository reviewRepository, ItemRepository itemRepository) {
    this.reviewRepository = reviewRepository;
    this.itemRepository = itemRepository;
  }

  /** 获取待审核商品队列 */
  public List<Map<String, Object>> getReviewQueue() {
    return itemRepository.findPending();
  }

  /** 审核通过 */
  public void approve(Long itemId, Long operatorId) {
    itemRepository.updateReviewStatus(itemId, "APPROVED", null);
    reviewRepository.save(itemId, operatorId, "APPROVED", null);
  }

  /** 审核驳回 */
  public void reject(Long itemId, Long operatorId, String reason) {
    itemRepository.updateReviewStatus(itemId, "REJECTED", reason);
    reviewRepository.save(itemId, operatorId, "REJECTED", reason);
  }

  /** 获取审核日志列表 */
  public List<Map<String, Object>> listReviewLogs() {
    return reviewRepository.findAll();
  }
}

package com.campus.marketplace.service;

import com.campus.marketplace.repository.ItemRepository;
import com.campus.marketplace.repository.UserRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ItemService {
  private final ItemRepository itemRepository;
  private final UserRepository userRepository;

  public ItemService(ItemRepository itemRepository, UserRepository userRepository) {
    this.itemRepository = itemRepository;
    this.userRepository = userRepository;
  }

  public Map<String, Object> createItem(Long sellerId, String title, Integer price,
      String description, Object imageUrls, String category,
      String conditionLevel, String campus) {
    var sellerOpt = userRepository.findById(sellerId);
    if (sellerOpt.isEmpty()) {
      throw new IllegalArgumentException("卖家不存在");
    }
    var seller = sellerOpt.get();
    // 兼容 string[] 和 JSON 字符串两种格式
    String imageUrlsStr;
    if (imageUrls instanceof List) {
      StringBuilder sb = new StringBuilder("[");
      List<?> list = (List<?>) imageUrls;
      for (int i = 0; i < list.size(); i++) {
        if (i > 0) sb.append(",");
        sb.append("\"").append(list.get(i).toString()).append("\"");
      }
      sb.append("]");
      imageUrlsStr = sb.toString();
    } else {
      imageUrlsStr = imageUrls != null ? imageUrls.toString() : null;
    }
    return itemRepository.save(title, price, description, sellerId,
        String.valueOf(seller.get("nickname")), imageUrlsStr, category, conditionLevel, campus);
  }

  // 向后兼容
  public Map<String, Object> createItem(Long sellerId, String title, Integer price,
      String description) {
    return createItem(sellerId, title, price, description, null, null, null, null);
  }

  public List<Map<String, Object>> listItems(Boolean approvedOnly, Boolean mine, Long userId) {
    boolean showMine = Boolean.TRUE.equals(mine);
    boolean showApprovedOnly = Boolean.TRUE.equals(approvedOnly);

    if (showMine && showApprovedOnly) {
      return itemRepository.findBySellerIdAndApproved(userId);
    } else if (showMine) {
      return itemRepository.findBySellerId(userId);
    } else if (showApprovedOnly) {
      return itemRepository.findApproved();
    }
    return itemRepository.findAll();
  }

  /** 分页查询商品 */
  public Map<String, Object> listItemsPaged(Boolean approvedOnly, Boolean mine,
      String keyword, String category, String sort,
      int pageNo, int pageSize, Long userId) {
    boolean showMine = Boolean.TRUE.equals(mine);
    boolean showApprovedOnly = Boolean.TRUE.equals(approvedOnly);

    String status = null;
    if (showApprovedOnly) status = "APPROVED";

    List<Map<String, Object>> rows;
    int total;

    if (showMine) {
      // 我的商品 — 用 SQL 层分页
      rows = itemRepository.findBySellerIdPaged(userId, null, pageNo, pageSize);
      total = itemRepository.countBySellerIdWithFilter(userId, null);
    } else {
      rows = itemRepository.findByPage(status, keyword, category, pageNo, pageSize);
      total = itemRepository.countByFilter(status, keyword, category);
    }

    return Map.of(
      "code", 200,
      "data", Map.of(
        "items", rows,
        "totalCount", total,
        "pageNo", pageNo,
        "pageSize", pageSize
      )
    );
  }

  /** 卖家我的商品（分页+状态筛选） */
  public Map<String, Object> listMyItemsPaged(Long sellerId, String status, int pageNo, int pageSize) {
    List<Map<String, Object>> rows = itemRepository.findBySellerIdPaged(sellerId, status, pageNo, pageSize);
    int total = itemRepository.countBySellerIdWithFilter(sellerId, status);

    return Map.of(
      "code", 200,
      "data", Map.of(
        "items", rows,
        "totalCount", total,
        "pageNo", pageNo,
        "pageSize", pageSize
      )
    );
  }

  public Map<String, Object> getItemDetail(Long id) {
    Map<String, Object> item = itemRepository.findById(id);
    if (item == null) {
      throw new IllegalArgumentException("商品不存在");
    }
    return item;
  }

  public void updateReviewResult(Long id, String status, String reason) {
    itemRepository.updateReviewStatus(id, status, reason);
  }

  /** 下架商品 */
  public void offShelfItem(Long itemId, Long sellerId) {
    Map<String, Object> item = itemRepository.findById(itemId);
    if (item == null) {
      throw new IllegalArgumentException("商品不存在");
    }
    if (!sellerId.equals(item.get("sellerId"))) {
      throw new IllegalArgumentException("只能下架自己的商品");
    }
    itemRepository.updateReviewStatus(itemId, "OFF_SHELF", "卖家主动下架");
  }
}

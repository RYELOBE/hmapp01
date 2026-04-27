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
      String description) {
    var sellerOpt = userRepository.findById(sellerId);
    if (sellerOpt.isEmpty()) {
      throw new IllegalArgumentException("卖家不存在");
    }
    var seller = sellerOpt.get();
    return itemRepository.save(title, price, description, sellerId,
        String.valueOf(seller.get("nickname")));
  }

  public List<Map<String, Object>> listItems(Boolean approvedOnly, Boolean mine, Long userId) {
    if (Boolean.TRUE.equals(mine)) {
      return itemRepository.findBySellerId(userId);
    } else if (Boolean.TRUE.equals(approvedOnly)) {
      return itemRepository.findApproved();
    }
    return itemRepository.findAll();
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
}

package com.campus.marketplace.service;

import com.campus.marketplace.repository.FavoriteRepository;
import com.campus.marketplace.repository.ItemRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class FavoriteService {

  private final FavoriteRepository favoriteRepository;
  private final ItemRepository itemRepository;

  public FavoriteService(FavoriteRepository favoriteRepository, ItemRepository itemRepository) {
    this.favoriteRepository = favoriteRepository;
    this.itemRepository = itemRepository;
  }

  public Map<String, Object> addFavorite(Long userId, Long itemId) {
    var item = itemRepository.findById(itemId);
    if (item == null) {
      throw new IllegalArgumentException("商品不存在");
    }

    if (favoriteRepository.existsByUserIdAndItemId(userId, itemId)) {
      return Map.of("code", 200, "message", "已收藏");
    }

    favoriteRepository.create(userId, itemId);
    return Map.of("code", 200, "message", "收藏成功");
  }

  public Map<String, Object> removeFavorite(Long userId, Long itemId) {
    if (!favoriteRepository.existsByUserIdAndItemId(userId, itemId)) {
      throw new IllegalArgumentException("未收藏此商品");
    }
    favoriteRepository.delete(userId, itemId);
    return Map.of("code", 200, "message", "已取消收藏");
  }

  public Map<String, Object> getFavoriteList(Long userId) {
    List<Map<String, Object>> favorites = favoriteRepository.findByUserId(userId);
    List<Map<String, Object>> result = new ArrayList<>();

    for (Map<String, Object> fav : favorites) {
      Long itemId = (Long) fav.get("itemId");
      var item = itemRepository.findById(itemId);
      if (item != null) {
        Map<String, Object> enriched = new java.util.HashMap<>(fav);
        enriched.put("item", item);
        enriched.put("isFavorited", true);
        result.add(enriched);
      }
    }

    return Map.of("code", 200, "data", result);
  }

  public Map<String, Object> checkFavorite(Long userId, Long itemId) {
    boolean isFavorited = favoriteRepository.existsByUserIdAndItemId(userId, itemId);
    return Map.of("code", 200, "data", Map.of("isFavorited", isFavorited));
  }

  public Map<String, Object> getFavoriteCount(Long userId) {
    int count = favoriteRepository.countByUserId(userId);
    return Map.of("code", 200, "data", Map.of("count", count));
  }
}

package com.campus.marketplace.service;

import com.campus.marketplace.repository.ItemStatsRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class ItemStatsService {

  private final ItemStatsRepository itemStatsRepository;

  public ItemStatsService(ItemStatsRepository itemStatsRepository) {
    this.itemStatsRepository = itemStatsRepository;
  }

  public Map<String, Object> getHotItems(int limit) {
    List<Map<String, Object>> items = itemStatsRepository.findHotItems(limit);
    Map<String, Object> result = new HashMap<>();
    result.put("code", 200);
    Map<String, Object> data = new HashMap<>();
    data.put("items", items);
    data.put("totalCount", items.size());
    result.put("data", data);
    return result;
  }

  public void trackView(Long itemId) {
    itemStatsRepository.incrementView(itemId);
  }

  public void trackClick(Long itemId) {
    itemStatsRepository.incrementClick(itemId);
  }

  public void trackFavorite(Long itemId, boolean isAdd) {
    if (isAdd) {
      itemStatsRepository.incrementFavorite(itemId);
    } else {
      itemStatsRepository.decrementFavorite(itemId);
    }
  }

  public void initStatsForNewItem(Long itemId) {
    itemStatsRepository.initStatsForItem(itemId);
  }

  public void batchInitStats() {
    itemStatsRepository.batchInitWithRandomStats();
  }
}

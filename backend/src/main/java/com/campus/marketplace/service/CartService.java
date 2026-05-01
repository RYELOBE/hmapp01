package com.campus.marketplace.service;

import com.campus.marketplace.repository.CartRepository;
import com.campus.marketplace.repository.ItemRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CartService {

  private final CartRepository cartRepository;
  private final ItemRepository itemRepository;

  public CartService(CartRepository cartRepository, ItemRepository itemRepository) {
    this.cartRepository = cartRepository;
    this.itemRepository = itemRepository;
  }

  public Map<String, Object> addToCart(Long userId, Long itemId, int quantity) {
    if (quantity < 1 || quantity > 99) {
      throw new IllegalArgumentException("数量必须在1-99之间");
    }

    var itemOpt = itemRepository.findById(itemId);
    if (itemOpt.isEmpty()) {
      throw new IllegalArgumentException("商品不存在");
    }

    Map<String, Object> item = itemOpt.get();
    if (!"APPROVED".equals(item.get("reviewStatus"))) {
      throw new IllegalArgumentException("商品未上架");
    }

    var existingOpt = cartRepository.findByUserIdAndItemId(userId, itemId);
    if (existingOpt.isPresent()) {
      Map<String, Object> existing = existingOpt.get();
      int newQuantity = (int) existing.get("quantity") + quantity;
      if (newQuantity > 99) {
        newQuantity = 99;
      }
      cartRepository.updateQuantity((Long) existing.get("id"), newQuantity);
      return Map.of("code", 200, "message", "已更新购物车数量", "data", cartRepository.findById((Long) existing.get("id")).orElseThrow());
    }

    Map<String, Object> cartItem = cartRepository.create(userId, itemId, quantity, true);
    return Map.of("code", 200, "message", "已加入购物车", "data", cartItem);
  }

  public Map<String, Object> getCartList(Long userId) {
    List<Map<String, Object>> cartItems = cartRepository.findByUserId(userId);
    List<Map<String, Object>> result = new ArrayList<>();
    int totalAmount = 0;

    for (Map<String, Object> cart : cartItems) {
      Long itemId = (Long) cart.get("itemId");
      var itemOpt = itemRepository.findById(itemId);
      if (itemOpt.isPresent()) {
        Map<String, Object> item = itemOpt.get();
        Map<String, Object> enrichedItem = new HashMap<>(cart);
        enrichedItem.put("item", item);
        if ((Boolean) cart.get("selected")) {
          totalAmount += (int) item.get("price") * (int) cart.get("quantity");
        }
        result.add(enrichedItem);
      }
    }

    return Map.of("code", 200, "data", result, "totalAmount", totalAmount);
  }

  public Map<String, Object> updateQuantity(Long userId, Long cartId, int quantity) {
    if (quantity < 1 || quantity > 99) {
      throw new IllegalArgumentException("数量必须在1-99之间");
    }

    var cartOpt = cartRepository.findById(cartId);
    if (cartOpt.isEmpty()) {
      throw new IllegalArgumentException("购物车项不存在");
    }
    Map<String, Object> cart = cartOpt.get();
    if (!userId.equals(cart.get("userId"))) {
      throw new IllegalArgumentException("无权操作此购物车项");
    }

    cartRepository.updateQuantity(cartId, quantity);
    return Map.of("code", 200, "message", "数量已更新", "data", cartRepository.findById(cartId).orElseThrow());
  }

  public Map<String, Object> updateSelected(Long userId, Long cartId, boolean selected) {
    var cartOpt = cartRepository.findById(cartId);
    if (cartOpt.isEmpty()) {
      throw new IllegalArgumentException("购物车项不存在");
    }
    Map<String, Object> cart = cartOpt.get();
    if (!userId.equals(cart.get("userId"))) {
      throw new IllegalArgumentException("无权操作此购物车项");
    }

    cartRepository.updateSelected(cartId, selected);
    return Map.of("code", 200, "message", "选中状态已更新");
  }

  public Map<String, Object> batchUpdateSelected(Long userId, List<Long> cartIds, boolean selected) {
    cartRepository.updateSelectedByUserIdAndItemIds(userId, cartIds, selected);
    return Map.of("code", 200, "message", "选中状态已更新");
  }

  public Map<String, Object> deleteItem(Long userId, Long cartId) {
    var cartOpt = cartRepository.findById(cartId);
    if (cartOpt.isEmpty()) {
      throw new IllegalArgumentException("购物车项不存在");
    }
    Map<String, Object> cart = cartOpt.get();
    if (!userId.equals(cart.get("userId"))) {
      throw new IllegalArgumentException("无权操作此购物车项");
    }

    cartRepository.delete(cartId);
    return Map.of("code", 200, "message", "已从购物车删除");
  }

  public Map<String, Object> clearCart(Long userId) {
    cartRepository.clearByUserId(userId);
    return Map.of("code", 200, "message", "购物车已清空");
  }

  public Map<String, Object> getCartCount(Long userId) {
    int count = cartRepository.countByUserId(userId);
    int selectedCount = cartRepository.countSelectedByUserId(userId);
    return Map.of("code", 200, "data", Map.of("total", count, "selected", selectedCount));
  }
}

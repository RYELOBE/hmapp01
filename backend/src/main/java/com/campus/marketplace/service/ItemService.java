package com.campus.marketplace.service;

import com.campus.marketplace.repository.ItemRepository;
import com.campus.marketplace.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemService {

  private final ItemRepository itemRepository;
  private final UserRepository userRepository;

  public ItemService(ItemRepository itemRepository, UserRepository userRepository) {
    this.itemRepository = itemRepository;
    this.userRepository = userRepository;
  }

  /**
   * 创建商品
   * @param sellerId 卖家ID
   * @param title 商品标题
   * @param price 商品价格
   * @param description 商品描述
   * @param imageUrls 图片URL列表
   * @param category 商品类别
   * @param conditionLevel 新旧程度
   * @param campus 校区
   * @return 创建的商品信息
   */
  public Map<String, Object> createItem(Long sellerId, String title, Integer price,
      String description, Object imageUrls, String category,
      String conditionLevel, String campus) {
    validateSeller(sellerId);
    String imageUrlsStr = parseImageUrls(imageUrls);

    return itemRepository.save(title, price, description, sellerId,
        getSellerNickname(sellerId), imageUrlsStr, category, conditionLevel, campus);
  }

  /**
   * 创建商品（向后兼容版本）
   */
  public Map<String, Object> createItem(Long sellerId, String title, Integer price,
      String description) {
    return createItem(sellerId, title, price, description, null, null, null, null);
  }

  /**
   * 查询商品列表
   * @param approvedOnly 仅显示已审核商品
   * @param mine 仅显示我的商品
   * @param userId 当前用户ID
   * @return 商品列表
   */
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

  /**
   * 分页查询商品
   * @param approvedOnly 仅显示已审核商品
   * @param mine 仅显示我的商品
   * @param keyword 搜索关键词
   * @param category 商品类别
   * @param sort 排序方式
   * @param pageNo 页码
   * @param pageSize 每页数量
   * @param userId 当前用户ID
   * @return 商品分页结果
   */
  public Map<String, Object> listItemsPaged(Boolean approvedOnly, Boolean mine,
      String keyword, String category, String sort,
      int pageNo, int pageSize, Long userId) {
    validatePageParams(pageNo, pageSize);

    boolean showMine = Boolean.TRUE.equals(mine);
    boolean showApprovedOnly = Boolean.TRUE.equals(approvedOnly);

    String status = null;
    if (showApprovedOnly) {
      status = "APPROVED";
    }

    List<Map<String, Object>> rows;
    int total;

    if (showMine) {
      rows = itemRepository.findBySellerIdPaged(userId, null, pageNo, pageSize);
      total = itemRepository.countBySellerIdWithFilter(userId, null);
    } else {
      rows = itemRepository.findByPage(status, keyword, category, pageNo, pageSize);
      total = itemRepository.countByFilter(status, keyword, category);
    }

    return buildSuccessResponse(Map.of(
      "items", rows,
      "totalCount", total,
      "pageNo", pageNo,
      "pageSize", pageSize
    ));
  }

  /**
   * 卖家我的商品（分页+状态筛选）
   * @param sellerId 卖家ID
   * @param status 商品状态
   * @param pageNo 页码
   * @param pageSize 每页数量
   * @return 商品分页结果
   */
  public Map<String, Object> listMyItemsPaged(Long sellerId, String status, int pageNo, int pageSize) {
    validatePageParams(pageNo, pageSize);
    validateSeller(sellerId);

    List<Map<String, Object>> rows = itemRepository.findBySellerIdPaged(sellerId, status, pageNo, pageSize);
    int total = itemRepository.countBySellerIdWithFilter(sellerId, status);

    return buildSuccessResponse(Map.of(
      "items", rows,
      "totalCount", total,
      "pageNo", pageNo,
      "pageSize", pageSize
    ));
  }

  /**
   * 获取商品详情
   * @param id 商品ID
   * @return 商品详情
   */
  public Map<String, Object> getItemDetail(Long id) {
    if (id == null) {
      throw new IllegalArgumentException("商品ID不能为空");
    }
    Map<String, Object> item = itemRepository.findById(id);
    if (item == null) {
      throw new IllegalArgumentException("商品不存在");
    }
    return item;
  }

  /**
   * 更新审核结果
   * @param id 商品ID
   * @param status 审核状态
   * @param reason 审核原因
   */
  public void updateReviewResult(Long id, String status, String reason) {
    if (id == null) {
      throw new IllegalArgumentException("商品ID不能为空");
    }
    if (status == null || status.isEmpty()) {
      throw new IllegalArgumentException("审核状态不能为空");
    }
    itemRepository.updateReviewStatus(id, status, reason);
  }

  /**
   * 下架商品
   * @param itemId 商品ID
   * @param sellerId 卖家ID
   */
  public void offShelfItem(Long itemId, Long sellerId) {
    validateSeller(sellerId);

    Map<String, Object> item = itemRepository.findById(itemId);
    if (item == null) {
      throw new IllegalArgumentException("商品不存在");
    }
    if (!sellerId.equals(item.get("sellerId"))) {
      throw new IllegalArgumentException("只能下架自己的商品");
    }
    itemRepository.updateReviewStatus(itemId, "OFF_SHELF", "卖家主动下架");
  }

  public Map<String, Object> updateItem(Long itemId, Long sellerId, String title, Integer price,
      String description, Object imageUrls, String category, String conditionLevel) {
    Map<String, Object> item = itemRepository.findById(itemId);
    if (item == null) {
      throw new IllegalArgumentException("商品不存在");
    }
    if (!sellerId.equals(item.get("sellerId"))) {
      throw new IllegalArgumentException("只能编辑自己的商品");
    }
    itemRepository.update(itemId, title, price, description, imageUrls, category, conditionLevel);
    return Map.of("code", 200, "message", "商品已更新", "data", itemRepository.findById(itemId));
  }

  public void deleteItem(Long itemId, Long sellerId) {
    Map<String, Object> item = itemRepository.findById(itemId);
    if (item == null) {
      throw new IllegalArgumentException("商品不存在");
    }
    if (!sellerId.equals(item.get("sellerId"))) {
      throw new IllegalArgumentException("只能删除自己的商品");
    }
    itemRepository.delete(itemId);
  }

  /**
   * 按日期范围统计商品数量
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @return 商品数量
   */
  public int countItemsByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
    if (startTime == null || endTime == null) {
      throw new IllegalArgumentException("时间范围不能为空");
    }
    return itemRepository.countItemsByDateRange(startTime, endTime);
  }

  /**
   * 统计今日发布商品数
   * @return 今日发布商品数量
   */
  public int countTodayItems() {
    return itemRepository.countTodayItems();
  }

  /**
   * 统计待审核商品数量
   * @return 待审核商品数量
   */
  public int countPendingItems() {
    return itemRepository.countPending();
  }

  /**
   * 按类别统计商品数量
   * @param category 商品类别
   * @return 商品数量
   */
  public int countByCategory(String category) {
    if (category == null || category.isEmpty()) {
      throw new IllegalArgumentException("商品类别不能为空");
    }
    return itemRepository.countByCategory(category);
  }

  /**
   * 获取卖家昵称
   */
  private String getSellerNickname(Long sellerId) {
    return userRepository.findById(sellerId)
        .map(u -> (String) u.get("nickname"))
        .orElse("未知用户");
  }

  /**
   * 验证卖家是否存在
   */
  private void validateSeller(Long sellerId) {
    if (sellerId == null) {
      throw new IllegalArgumentException("卖家ID不能为空");
    }
    if (userRepository.findById(sellerId).isEmpty()) {
      throw new IllegalArgumentException("卖家不存在");
    }
  }

  /**
   * 验证分页参数
   */
  private void validatePageParams(int pageNo, int pageSize) {
    if (pageNo < 1) {
      throw new IllegalArgumentException("页码必须大于0");
    }
    if (pageSize < 1 || pageSize > 100) {
      throw new IllegalArgumentException("每页数量必须在1-100之间");
    }
  }

  /**
   * 解析图片URL列表
   */
  private String parseImageUrls(Object imageUrls) {
    if (imageUrls == null) {
      return null;
    }
    if (imageUrls instanceof List) {
      StringBuilder sb = new StringBuilder("[");
      List<?> list = (List<?>) imageUrls;
      for (int i = 0; i < list.size(); i++) {
        if (i > 0) {
          sb.append(",");
        }
        sb.append("\"").append(list.get(i).toString()).append("\"");
      }
      sb.append("]");
      return sb.toString();
    }
    return imageUrls.toString();
  }

  /**
   * 构建成功响应
   */
  private Map<String, Object> buildSuccessResponse(Object data) {
    return Map.of("code", 200, "data", data);
  }
}

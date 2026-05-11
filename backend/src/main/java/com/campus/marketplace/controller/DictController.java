package com.campus.marketplace.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dict")
public class DictController {

  @GetMapping("/options")
  public Map<String, Object> getOptions() {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("categories", getCategories());
    data.put("conditions", getConditions());
    data.put("campuses", getCampuses());
    data.put("tags", getTags());
    data.put("userStatuses", getUserStatuses());
    data.put("userRoles", getUserRoles());
    data.put("reviewStatuses", getReviewStatuses());
    data.put("itemStatuses", getItemStatuses());
    data.put("orderStatuses", getOrderStatuses());
    data.put("reviewModerationStatuses", getReviewModerationStatuses());
    data.put("reviewTypes", getReviewTypes());
    data.put("permissions", getPermissions());
    return buildSuccessResponse(data);
  }

  private List<Map<String, String>> getCategories() {
    List<Map<String, String>> list = new ArrayList<>();
    add(list, "ELECTRONICS", "电子产品", "📱");
    add(list, "BOOKS", "图书教材", "📚");
    add(list, "CLOTHING", "服饰鞋包", "👔");
    add(list, "DAILY", "生活用品", "🏠");
    add(list, "SPORTS", "运动器材", "⚽");
    add(list, "BEAUTY", "美妆护肤", "💄");
    add(list, "FOOD", "食品零食", "🍪");
    add(list, "OTHER", "其他物品", "🎁");
    return list;
  }

  private List<Map<String, String>> getConditions() {
    List<Map<String, String>> list = new ArrayList<>();
    add2(list, "NEW", "全新");
    add2(list, "LIKE_NEW", "99新");
    add2(list, "EXCELLENT", "95新");
    add2(list, "GOOD", "8成新");
    add2(list, "FAIR", "7成新");
    add2(list, "POOR", "较差");
    return list;
  }

  private List<Map<String, String>> getCampuses() {
    List<Map<String, String>> list = new ArrayList<>();
    add2(list, "", "全部校区");
    add2(list, "主校区", "主校区");
    add2(list, "东校区", "东校区");
    add2(list, "西校区", "西校区");
    return list;
  }

  private List<Map<String, String>> getTags() {
    List<Map<String, String>> list = new ArrayList<>();
    add2(list, "#学习资料", "#学习资料");
    add2(list, "#生活好物", "#生活好物");
    add2(list, "#闲置转让", "#闲置转让");
    add2(list, "#经验分享", "#经验分享");
    add2(list, "#求助问答", "#求助问答");
    return list;
  }

  private List<Map<String, Object>> getUserStatuses() {
    List<Map<String, Object>> list = new ArrayList<>();
    addWithColor(list, "", "全部状态", "");
    addWithColor(list, "ACTIVE", "正常", "green");
    addWithColor(list, "DISABLED", "禁用", "red");
    return list;
  }

  private List<Map<String, Object>> getUserRoles() {
    List<Map<String, Object>> list = new ArrayList<>();
    addWithColor(list, "", "全部角色", "");
    addWithColor(list, "BUYER", "买家", "arcoblue");
    addWithColor(list, "SELLER", "卖家", "orangered");
    addWithColor(list, "BOTH", "两者都是", "purple");
    addWithColor(list, "OPS", "运营管理员", "purple");
    addWithColor(list, "OPS_ADMIN", "运营管理员", "purple");
    addWithColor(list, "OPS_SUPER", "超级管理员", "red");
    return list;
  }

  private List<Map<String, Object>> getReviewStatuses() {
    List<Map<String, Object>> list = new ArrayList<>();
    addWithColor(list, "PENDING_REVIEW", "待审核", "orangered");
    addWithColor(list, "APPROVED", "已通过", "green");
    addWithColor(list, "REJECTED", "已拒绝", "red");
    return list;
  }

  private List<Map<String, Object>> getItemStatuses() {
    List<Map<String, Object>> list = new ArrayList<>();
    addWithColor(list, "", "全部状态", "");
    addWithColor(list, "APPROVED", "在售", "green");
    addWithColor(list, "SOLD", "已售", "arcoblue");
    addWithColor(list, "OFFLINE", "已下架", "gray");
    addWithColor(list, "PENDING_REVIEW", "待审核", "orangered");
    addWithColor(list, "REJECTED", "已拒绝", "red");
    return list;
  }

  private List<Map<String, Object>> getOrderStatuses() {
    List<Map<String, Object>> list = new ArrayList<>();
    addWithColor(list, "", "全部状态", "");
    addWithColor(list, "PENDING_PAYMENT", "待支付", "orange");
    addWithColor(list, "PAID", "待交易", "arcoblue");
    addWithColor(list, "COMPLETED", "已完成", "green");
    addWithColor(list, "CANCELLED", "已取消", "gray");
    addWithColor(list, "REFUNDING", "退款中", "orangered");
    addWithColor(list, "REFUNDED", "已退款", "gray");
    return list;
  }

  private List<Map<String, Object>> getReviewModerationStatuses() {
    List<Map<String, Object>> list = new ArrayList<>();
    addWithColor(list, "", "全部状态", "");
    addWithColor(list, "PENDING", "待审核", "orangered");
    addWithColor(list, "APPROVED", "已通过", "green");
    addWithColor(list, "REJECTED", "已拒绝", "red");
    return list;
  }

  private List<Map<String, Object>> getReviewTypes() {
    List<Map<String, Object>> list = new ArrayList<>();
    addWithColor(list, "", "全部类型", "");
    addWithColor(list, "ITEM", "商品评价", "arcoblue");
    addWithColor(list, "CIRCLE", "圈子评价", "purple");
    return list;
  }

  private List<Map<String, Object>> getPermissions() {
    List<Map<String, Object>> list = new ArrayList<>();
    
    // 商品管理权限
    addPermission(list, "item:view", "浏览商品", "📦");
    addPermission(list, "item:create", "发布商品", "📦");
    addPermission(list, "item:edit", "编辑商品", "📦");
    addPermission(list, "item:delete", "删除商品", "📦");
    addPermission(list, "item:review", "审核商品", "📦");
    
    // 订单管理权限
    addPermission(list, "order:create", "创建订单", "🛒");
    addPermission(list, "order:view", "查看订单", "🛒");
    addPermission(list, "order:cancel", "取消订单", "🛒");
    addPermission(list, "order:confirm", "确认订单", "🛒");
    addPermission(list, "order:refund", "退款操作", "🛒");
    
    // 用户管理权限
    addPermission(list, "user:view", "查看用户", "👤");
    addPermission(list, "user:edit", "编辑用户", "👤");
    addPermission(list, "user:disable", "禁用用户", "👤");
    addPermission(list, "user:assign_role", "角色分配", "👤");
    
    // 运营管理权限
    addPermission(list, "ops:review", "内容审核", "⚙️");
    addPermission(list, "ops:config", "系统配置", "⚙️");
    addPermission(list, "ops:stats", "数据统计", "⚙️");
    addPermission(list, "ops:announce", "公告管理", "⚙️");
    
    // 系统管理权限
    addPermission(list, "system:role_manage", "角色管理", "🔐");
    addPermission(list, "system:log_view", "日志查看", "🔐");
    addPermission(list, "system:menu_manage", "菜单管理", "🔐");
    
    return list;
  }

  private void add(List<Map<String, String>> list, String value, String label, String icon) {
    Map<String, String> item = new LinkedHashMap<>();
    item.put("value", value);
    item.put("label", label);
    item.put("icon", icon);
    list.add(item);
  }

  private void add2(List<Map<String, String>> list, String value, String label) {
    Map<String, String> item = new LinkedHashMap<>();
    item.put("value", value);
    item.put("label", label);
    list.add(item);
  }

  private void addWithColor(List<Map<String, Object>> list, String value, String label, String color) {
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("value", value);
    item.put("label", label);
    item.put("color", color);
    list.add(item);
  }

  private void addPermission(List<Map<String, Object>> list, String value, String label, String icon) {
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("value", value);
    item.put("label", label);
    item.put("icon", icon);
    list.add(item);
  }

  private Map<String, Object> buildSuccessResponse(Map<String, Object> data) {
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("code", 200);
    response.put("message", "success");
    response.put("data", data);
    return response;
  }
}

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
    return buildSuccessResponse(Map.of(
      "categories", getCategories(),
      "conditions", getConditions(),
      "campuses", getCampuses(),
      "tags", getTags()
    ));
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

  private Map<String, Object> buildSuccessResponse(Map<String, Object> data) {
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("code", 200);
    response.put("message", "success");
    response.put("data", data);
    return response;
  }
}

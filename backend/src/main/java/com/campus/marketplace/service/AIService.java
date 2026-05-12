package com.campus.marketplace.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.campus.marketplace.repository.AIMessageRepository;
import com.campus.marketplace.repository.AISessionRepository;
import com.campus.marketplace.repository.KnowledgeRepository;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.FavoriteRepository;
import com.campus.marketplace.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Service
public class AIService {
  private static final Logger logger = LoggerFactory.getLogger(AIService.class);

  private final AISessionRepository sessionRepository;
  private final AIMessageRepository messageRepository;
  private final KnowledgeRepository knowledgeRepository;
  private final OrderRepository orderRepository;
  private final FavoriteRepository favoriteRepository;
  private final CartRepository cartRepository;
  private final RestClient restClient;
  private final ObjectMapper objectMapper;
  private final String apiKey;
  private final String model;

  public AIService(
      AISessionRepository sessionRepository,
      AIMessageRepository messageRepository,
      KnowledgeRepository knowledgeRepository,
      OrderRepository orderRepository,
      FavoriteRepository favoriteRepository,
      CartRepository cartRepository,
      @Value("${ai.base-url}") String baseUrl,
      @Value("${ai.api-key}") String apiKey,
      @Value("${ai.model}") String model) {
    this.sessionRepository = sessionRepository;
    this.messageRepository = messageRepository;
    this.knowledgeRepository = knowledgeRepository;
    this.orderRepository = orderRepository;
    this.favoriteRepository = favoriteRepository;
    this.cartRepository = cartRepository;
    this.apiKey = apiKey;
    this.model = model;
    this.restClient = RestClient.create(baseUrl + "/api/paas/v4");
    this.objectMapper = new ObjectMapper();
  }

  public Map<String, Object> chat(Long userId, String sessionId, String role, String message) {
    logger.info("========================================");
    logger.info("🤖 AI 对话请求");
    logger.info("👤 用户ID: {}", userId);
    logger.info("💬 消息内容: {}", message.length() > 50 ? message.substring(0, 50) + "..." : message);
    logger.info("🏷️ AI模型: {}", model);

    String currentSessionId = sessionRepository.createOrReuseSession(sessionId, userId);
    sessionRepository.updateSessionTime(currentSessionId);

    // 1. 搜索知识库
    var knowledgeChunks = knowledgeRepository.search(message);
    List<String> references = knowledgeChunks.stream()
        .map(chunk -> chunk.get("title") + ": " + chunk.get("content"))
        .toList();

    // 2. 搜索商品数据（如果问题涉及商品）
    List<String> itemReferences = searchItemsForAI(message);
    references = new java.util.ArrayList<>(references);
    references.addAll(itemReferences);

    // 3. 查询用户数据（如果问题涉及用户个人数据）
    List<String> userDataReferences = searchUserDataForAI(userId, message);
    references.addAll(userDataReferences);

    String referencesJson = objectMapper.valueToTree(references).toString();
    messageRepository.save(currentSessionId, "user", message, referencesJson);

    // 用第一条消息更新会话标题
    sessionRepository.updateSessionTitle(currentSessionId, message);

    try {
      String answer = callRealAI(message, role, references);
      messageRepository.save(currentSessionId, "assistant", answer, null);

      return Map.of(
          "code", 200,
          "sessionId", currentSessionId,
          "reply", answer,
          "references", references
        );
    } catch (Exception e) {
      logger.error("❌ AI调用失败: {}", e.getMessage());
      return Map.of(
          "code", 500,
          "message", "AI服务暂时不可用: " + extractErrorMessage(e.getMessage()),
          "sessionId", currentSessionId
      );
    }
  }

  /**
   * 根据用户问题搜索商品
   */
  private List<String> searchItemsForAI(String message) {
    String lowerMsg = message.toLowerCase();

    // 判断是否需要搜索商品
    boolean needItemSearch = lowerMsg.contains("商品") || lowerMsg.contains("买") ||
        lowerMsg.contains("卖") || lowerMsg.contains("价格") || lowerMsg.contains("推荐") ||
        lowerMsg.contains("有什么") || lowerMsg.contains("有没有") ||
        lowerMsg.contains("搜索") || lowerMsg.contains("查找") ||
        lowerMsg.contains("全部") || lowerMsg.contains("哪些") || lowerMsg.contains("列表") ||
        lowerMsg.contains("数码") || lowerMsg.contains("电脑") || lowerMsg.contains("手机") ||
        lowerMsg.contains("书") || lowerMsg.contains("教材") || lowerMsg.contains("电器") ||
        lowerMsg.contains("生活") || lowerMsg.contains("运动") || lowerMsg.contains("服装") ||
        lowerMsg.contains("闲置") || lowerMsg.contains("二手");

    if (!needItemSearch) {
      return List.of();
    }

    // 如果问的是"全部商品"或"有哪些商品"，返回热门商品
    if (lowerMsg.contains("全部") || lowerMsg.contains("哪些") || lowerMsg.contains("有什么")) {
      var hotItems = knowledgeRepository.getHotItems();
      if (!hotItems.isEmpty()) {
        logger.info("📦 返回 {} 个热门商品", hotItems.size());
        return hotItems.stream()
            .map(item -> {
              String title = (String) item.get("title");
              Integer price = (Integer) item.get("price");
              String description = (String) item.get("description");
              String category = (String) item.get("category");
              String conditionLevel = (String) item.get("conditionLevel");
              String sellerName = (String) item.get("sellerName");

              return String.format("【商品】%s - %d元 | 分类:%s 成色:%s 卖家:%s | 描述:%s",
                  title, price, category != null ? category : "未分类",
                  conditionLevel != null ? conditionLevel : "未知",
                  sellerName != null ? sellerName : "匿名",
                  description != null ? description : "无描述");
            })
            .toList();
      }
    }

    // 搜索商品
    var items = knowledgeRepository.searchItems(message);
    if (items.isEmpty()) {
      logger.info("📦 未找到相关商品，返回热门商品");
      var hotItems = knowledgeRepository.getHotItems();
      return hotItems.stream()
          .map(item -> {
            String title = (String) item.get("title");
            Integer price = (Integer) item.get("price");
            String description = (String) item.get("description");
            String category = (String) item.get("category");
            String conditionLevel = (String) item.get("conditionLevel");
            String sellerName = (String) item.get("sellerName");

            return String.format("【商品】%s - %d元 | 分类:%s 成色:%s 卖家:%s | 描述:%s",
                title, price, category != null ? category : "未分类",
                conditionLevel != null ? conditionLevel : "未知",
                sellerName != null ? sellerName : "匿名",
                description != null ? description : "无描述");
          })
          .toList();
    }

    logger.info("📦 找到 {} 个相关商品", items.size());
    return items.stream()
        .map(item -> {
          String title = (String) item.get("title");
          Integer price = (Integer) item.get("price");
          String description = (String) item.get("description");
          String category = (String) item.get("category");
          String conditionLevel = (String) item.get("conditionLevel");
          String sellerName = (String) item.get("sellerName");

          return String.format("【商品】%s - %d元 | 分类:%s 成色:%s 卖家:%s | 描述:%s",
              title, price, category != null ? category : "未分类",
              conditionLevel != null ? conditionLevel : "未知",
              sellerName != null ? sellerName : "匿名",
              description != null ? description : "无描述");
        })
        .toList();
  }

  /**
   * 查询用户个人数据（订单、收藏、购物车）
   */
  private List<String> searchUserDataForAI(Long userId, String message) {
    List<String> references = new java.util.ArrayList<>();
    String lowerMsg = message.toLowerCase();

    // 查询订单
    if (lowerMsg.contains("订单") || lowerMsg.contains("购买记录") || lowerMsg.contains("买了什么")) {
      var orders = orderRepository.findByBuyerId(userId);
      if (!orders.isEmpty()) {
        logger.info("📋 找到 {} 条订单记录", orders.size());
        references.add(String.format("【用户订单】共%d条订单记录", orders.size()));
        for (int i = 0; i < Math.min(3, orders.size()); i++) {
          Map<String, Object> order = orders.get(i);
          references.add(String.format("订单:%s | 状态:%s | 金额:%s元",
              order.get("orderNo"), order.get("status"), order.get("totalAmount")));
        }
      }
    }

    // 查询收藏
    if (lowerMsg.contains("收藏") || lowerMsg.contains("喜欢") || lowerMsg.contains("关注")) {
      var favorites = favoriteRepository.findByUserId(userId);
      if (!favorites.isEmpty()) {
        logger.info("❤️ 找到 {} 条收藏记录", favorites.size());
        references.add(String.format("【用户收藏】共%d件收藏商品", favorites.size()));
      }
    }

    // 查询购物车
    if (lowerMsg.contains("购物车") || lowerMsg.contains("待购")) {
      var cartItems = cartRepository.findByUserId(userId);
      if (!cartItems.isEmpty()) {
        logger.info("🛒 找到 {} 条购物车记录", cartItems.size());
        references.add(String.format("【购物车】共%d件商品", cartItems.size()));
      }
    }

    return references;
  }

  private String extractErrorMessage(String originalError) {
    if (originalError == null) return "未知错误";
    if (originalError.contains("余额不足")) return "AI服务余额不足，请联系管理员";
    if (originalError.contains("429")) return "AI服务请求过于频繁，请稍后再试";
    if (originalError.contains("timeout") || originalError.contains("超时")) return "AI服务响应超时，请稍后再试";
    return originalError.length() > 100 ? originalError.substring(0, 100) : originalError;
  }

  private String callRealAI(String message, String role, List<String> references) {
    logger.info("🤖 调用智谱AI API - 模型: {}", model);
    logger.info("👤 用户角色: {}", role);
    logger.info("💬 用户消息: {}", message.length() > 50 ? message.substring(0, 50) + "..." : message);
    logger.info("📚 知识库条数: {}", references.size());

    StringBuilder systemPrompt = new StringBuilder();
    systemPrompt.append("你是校园闲置物品流转平台的智能助手，可以直接访问平台的商品数据库。\n");
    systemPrompt.append("当用户询问商品时，请根据提供的商品信息直接回答，不要说无法访问数据库。\n\n");

    if ("OPS".equalsIgnoreCase(role)) {
      systemPrompt.append("当前用户是运营人员，负责审核商品。\n");
    } else if ("SELLER".equalsIgnoreCase(role)) {
      systemPrompt.append("当前用户是卖家，可以发布和管理商品。\n");
    } else {
      systemPrompt.append("当前用户是买家，可以浏览和购买商品。\n");
    }

    systemPrompt.append("\n=== 平台数据 ===\n");
    for (String ref : references) {
      systemPrompt.append(ref).append("\n");
    }

    systemPrompt.append("\n=== 回答要求 ===\n");
    systemPrompt.append("1. 如果用户问商品相关问题，请直接根据上面的商品数据回答\n");
    systemPrompt.append("2. 列出商品时，请包含商品名称、价格、分类等关键信息\n");
    systemPrompt.append("3. 如果数据中没有相关商品，请告诉用户目前没有这类商品\n");
    systemPrompt.append("4. 回答要简洁友好，适当使用emoji\n");

    ObjectNode requestBody = objectMapper.createObjectNode();
    requestBody.put("model", model);
    requestBody.put("temperature", 0.7);
    requestBody.put("stream", false);

    ArrayNode messages = requestBody.putArray("messages");
    ObjectNode systemMessage = messages.addObject();
    systemMessage.put("role", "system");
    systemMessage.put("content", systemPrompt.toString());

    ObjectNode userMessage = messages.addObject();
    userMessage.put("role", "user");
    userMessage.put("content", message);

    logger.info("📤 请求体: {}", requestBody.toString().length() > 500 ? requestBody.toString().substring(0, 500) + "..."
        : requestBody.toString());

    try {
      long startTime = System.currentTimeMillis();
      String response = restClient.post()
          .uri("/chat/completions")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
          .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .body(requestBody.toString())
          .retrieve()
          .body(String.class);
      long duration = System.currentTimeMillis() - startTime;

      logger.info("✅ AI API 调用成功 - 耗时: {}ms", duration);
      logger.info("📦 API 原始响应: {}", response.length() > 500 ? response.substring(0, 500) + "..." : response);

      return extractAssistantResponse(response);
    } catch (RestClientException e) {
      logger.error("❌ AI API 调用失败 - HTTP错误: {} - {}", e.getClass().getSimpleName(), e.getMessage());
      throw new RuntimeException("AI服务暂时不可用，请稍后重试: " + e.getMessage(), e);
    } catch (Exception e) {
      logger.error("❌ AI 处理异常: {} - {}", e.getClass().getName(), e.getMessage());
      throw new RuntimeException("AI处理异常: " + e.getMessage(), e);
    }
  }

  public List<Map<String, Object>> getRecentSessions(Long userId) {
    return sessionRepository.findRecentByUserId(userId);
  }

  public List<Map<String, Object>> getMessages(String sessionId) {
    return messageRepository.findBySessionId(sessionId);
  }

  /**
   * 创建新会话
   */
  public String createNewSession(Long userId) {
    String sessionId = "S" + System.currentTimeMillis();
    sessionRepository.createSession(sessionId, userId);
    logger.info("📝 创建新会话: {} for user: {}", sessionId, userId);
    return sessionId;
  }

  /**
   * 删除会话
   */
  public void deleteSession(String sessionId, Long userId) {
    sessionRepository.deleteSession(sessionId, userId);
    messageRepository.deleteBySessionId(sessionId);
    logger.info("🗑️ 删除会话: {} for user: {}", sessionId, userId);
  }

  public List<String> getPresets(String role) {
    if ("OPS".equalsIgnoreCase(role)) {
      return List.of("如何审核商品？", "驳回商品需要填写什么？", "哪些商品不允许上架？");
    } else if ("SELLER".equalsIgnoreCase(role)) {
      return List.of("如何发布商品？", "审核需要多久？", "商品被驳回怎么办？");
    } else {
      return List.of("如何购买商品？", "订单流程是怎样的？", "平台规则是什么？");
    }
  }

  private String extractAssistantResponse(String responseJson) {
    try {
      JsonNode root = objectMapper.readTree(responseJson);
      JsonNode choices = root.path("choices");
      if (choices.isArray() && !choices.isEmpty()) {
        JsonNode content = choices.get(0).path("message").path("content");
        if (!content.isMissingNode() && !content.asText().isEmpty()) {
          String answer = content.asText();
          logger.info("💡 AI 回复: {}", answer.length() > 100 ? answer.substring(0, 100) + "..." : answer);
          return answer;
        }
      }
      logger.warn("⚠️ 无法解析AI回复，原始响应: {}", responseJson);
      throw new RuntimeException("AI返回格式异常");
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      logger.error("❌ 解析AI响应失败: {}", e.getMessage());
      throw new RuntimeException("AI响应解析失败: " + e.getMessage(), e);
    }
  }
}

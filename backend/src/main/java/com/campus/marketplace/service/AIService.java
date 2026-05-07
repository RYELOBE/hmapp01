package com.campus.marketplace.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.campus.marketplace.repository.AIMessageRepository;
import com.campus.marketplace.repository.AISessionRepository;
import com.campus.marketplace.repository.KnowledgeRepository;
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
  private final RestClient restClient;
  private final ObjectMapper objectMapper;
  private final String apiKey;
  private final String model;

  public AIService(
      AISessionRepository sessionRepository,
      AIMessageRepository messageRepository,
      KnowledgeRepository knowledgeRepository,
      @Value("${ai.base-url}") String baseUrl,
      @Value("${ai.api-key}") String apiKey,
      @Value("${ai.model}") String model) {
    this.sessionRepository = sessionRepository;
    this.messageRepository = messageRepository;
    this.knowledgeRepository = knowledgeRepository;
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

    var knowledgeChunks = knowledgeRepository.search(message);
    List<String> references = knowledgeChunks.stream()
        .map(chunk -> chunk.get("title") + ": " + chunk.get("content"))
        .toList();

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

    StringBuilder systemPrompt = new StringBuilder("你是一个校园二手交易平台的智能助手。");
    if ("OPS".equalsIgnoreCase(role)) {
      systemPrompt.append("当前用户是运营人员，负责审核商品。");
    } else if ("SELLER".equalsIgnoreCase(role)) {
      systemPrompt.append("当前用户是卖家，可以发布和管理商品。");
    } else {
      systemPrompt.append("当前用户是买家，可以浏览和购买商品。");
    }
    systemPrompt.append("请根据以下知识回答用户的问题：\n");
    for (String ref : references) {
      systemPrompt.append("- ").append(ref).append("\n");
    }
    systemPrompt.append("\n如果问题不在知识范围内，请礼貌地告诉用户。");

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

    logger.info("📤 请求体: {}", requestBody.toString().length() > 300 ? requestBody.toString().substring(0, 300) + "..."
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

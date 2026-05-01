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

  /**
   * 发起 AI 对话。
   *
   * @param userId    当前登录用户 ID
   * @param sessionId 可空，为空则新建会话
   * @param role      用户角色（BUYER / SELLER / OPS），用于定制系统提示
   * @param message   用户消息内容
   */
  public Map<String, Object> chat(Long userId, String sessionId, String role, String message) {
    String currentSessionId = sessionRepository.createOrReuseSession(sessionId, userId);
    sessionRepository.updateSessionTime(currentSessionId);

    // 搜索相关知识
    var knowledgeChunks = knowledgeRepository.search(message);
    List<String> references = knowledgeChunks.stream()
        .map(chunk -> chunk.get("title") + ": " + chunk.get("content"))
        .toList();

    // 构建系统提示（带知识库上下文）
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

    // 构建请求体
    ObjectNode requestBody = objectMapper.createObjectNode();
    requestBody.put("model", model);

    ArrayNode messages = requestBody.putArray("messages");
    ObjectNode systemMessage = messages.addObject();
    systemMessage.put("role", "system");
    systemMessage.put("content", systemPrompt.toString());

    ObjectNode userMessage = messages.addObject();
    userMessage.put("role", "user");
    userMessage.put("content", message);

    // 保存用户消息
    String referencesJson = objectMapper.valueToTree(references).toString();
    messageRepository.save(currentSessionId, "user", message, referencesJson);

    // 调用智谱 AI API
    String answer;
    try {
      String response = restClient.post()
          .uri("/chat/completions")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
          .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .body(requestBody.toString())
          .retrieve()
          .body(String.class);
      answer = extractAssistantResponse(response);
    } catch (RestClientException e) {
      logger.error("AI API call failed: {}", e.getMessage(), e);
      answer = "抱歉，AI 服务暂时不可用，请稍后重试。";
    }

    // 保存助手回复
    messageRepository.save(currentSessionId, "assistant", answer, null);

    return Map.of(
        "sessionId", currentSessionId,
        "answer", answer,
        "references", references
    );
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
        return content.asText("抱歉，我无法回答这个问题。");
      }
      return "抱歉，我无法回答这个问题。";
    } catch (Exception e) {
      return "抱歉，我无法回答这个问题。";
    }
  }
}

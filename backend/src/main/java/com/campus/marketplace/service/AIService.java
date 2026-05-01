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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
  private final boolean useMockAI;
  
  // 模拟 AI 响应模板
  private static final Map<String, String> MOCK_RESPONSES = new HashMap<>();
  static {
    MOCK_RESPONSES.put("如何购买商品", "**购买商品步骤**：\n\n1. 浏览商品列表或使用搜索功能找到心仪的商品\n2. 点击商品进入详情页，查看商品信息和卖家详情\n3. 点击\"立即购买\"按钮，进入订单确认页面\n4. 填写收货地址并确认订单信息\n5. 提交订单完成购买\n\n> 提示：买家下单后可在\"我的订单\"查看订单状态");
    MOCK_RESPONSES.put("订单流程是怎样的", "**订单流程说明**：\n\n1. **待付款**：订单创建后等待买家付款\n2. **已付款**：买家付款成功，等待卖家发货\n3. **已发货**：卖家已发货，买家等待收货\n4. **已完成**：买家确认收货，交易完成\n5. **退款中**：买家申请退款，处理中\n6. **已退款**：退款完成\n\n> 注意：待付款订单48小时后会自动取消");
    MOCK_RESPONSES.put("平台规则是什么", "**平台规则**：\n\n- 卖家发布商品后必须经过运营审核通过才可被买家看到\n- 商品审核状态有：待审核、审核通过、审核驳回\n- 驳回时运营会填写驳回原因，卖家可修改后重新提交\n- 买家下单后可在\"我的订单\"查看状态\n- 卖家可在订单页查看待发货列表\n\n> 提醒：请遵守平台规则，共同维护良好的交易环境");
    MOCK_RESPONSES.put("如何发布商品", "**发布商品步骤**：\n\n1. 登录账号并确保拥有卖家角色\n2. 进入卖家中心，点击\"发布商品\"\n3. 填写商品基本信息：标题、价格、分类、成色等\n4. 上传商品图片（最多9张）\n5. 填写商品详细描述\n6. 提交审核，等待运营人员审核\n\n> 提示：审核通过后商品即可在前台展示");
    MOCK_RESPONSES.put("审核需要多久", "**审核时间说明**：\n\n商品审核通常在 **24小时内** 完成。\n\n- 审核通过：商品立即上架展示\n- 审核驳回：会收到驳回原因通知，可修改后重新提交\n\n如果您的商品超过24小时未审核，可以联系客服查询进度。");
    MOCK_RESPONSES.put("商品被驳回怎么办", "**商品被驳回后的处理**：\n\n1. 查看驳回原因：在卖家中心找到被驳回的商品，查看运营填写的驳回原因\n2. 修改商品信息：根据驳回原因修改商品信息\n3. 重新提交审核：修改完成后重新提交审核\n\n**常见驳回原因**：\n- 信息不完整\n- 图片不规范\n- 价格异常\n- 商品违规");
    MOCK_RESPONSES.put("如何审核商品", "**商品审核流程**：\n\n1. 运营人员登录运营后台\n2. 进入\"商品审核\"页面查看待审核商品列表\n3. 点击商品查看详细信息和卖家历史\n4. 审核通过或审核拒绝\n5. 拒绝时需要填写拒绝原因\n\n审核结果会通过系统通知告知卖家。");
    MOCK_RESPONSES.put("驳回商品需要填写什么", "**驳回商品必填信息**：\n\n驳回商品时，运营人员必须填写 **拒绝原因**。\n\n**常见拒绝原因示例**：\n- 信息不完整，请补充商品描述\n- 图片不清晰，请重新上传\n- 价格异常，请核实价格\n- 商品违规，禁止发布\n\n拒绝原因会通知给卖家，帮助卖家修改后重新提交。");
    MOCK_RESPONSES.put("哪些商品不允许上架", "**禁止上架商品**：\n\n以下类型的商品不允许在平台上架：\n\n1. 违规违法商品\n2. 虚假商品或假冒伪劣商品\n3. 违反学校规定的商品\n4. 危险物品\n5. 其他平台禁止的商品\n\n运营人员审核时会严格把控商品质量，保障买家权益。");
  }

  public AIService(
      AISessionRepository sessionRepository,
      AIMessageRepository messageRepository,
      KnowledgeRepository knowledgeRepository,
      @Value("${ai.base-url}") String baseUrl,
      @Value("${ai.api-key}") String apiKey,
      @Value("${ai.model}") String model,
      @Value("${ai.use-mock:false}") boolean useMockAI) {
    this.sessionRepository = sessionRepository;
    this.messageRepository = messageRepository;
    this.knowledgeRepository = knowledgeRepository;
    this.apiKey = apiKey;
    this.model = model;
    this.useMockAI = useMockAI;
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

    // 保存用户消息
    String referencesJson = objectMapper.valueToTree(references).toString();
    messageRepository.save(currentSessionId, "user", message, referencesJson);

    // 获取 AI 回复
    String answer;
    if (useMockAI) {
      answer = getMockResponse(message, role);
    } else {
      answer = callRealAI(message, role, references);
    }

    // 保存助手回复
    messageRepository.save(currentSessionId, "assistant", answer, null);

    return Map.of(
        "sessionId", currentSessionId,
        "answer", answer,
        "references", references
    );
  }
  
  /**
   * 获取模拟 AI 响应
   */
  private String getMockResponse(String message, String role) {
    // 精确匹配预设问题
    for (Map.Entry<String, String> entry : MOCK_RESPONSES.entrySet()) {
      if (message.contains(entry.getKey())) {
        return entry.getValue();
      }
    }
    
    // 根据角色返回通用回复
    String roleDesc;
    if ("OPS".equalsIgnoreCase(role)) {
      roleDesc = "运营人员";
    } else if ("SELLER".equalsIgnoreCase(role)) {
      roleDesc = "卖家";
    } else {
      roleDesc = "买家";
    }
    
    return String.format(
        "您好！我是校园二手交易平台的AI助手。\n\n" +
        "作为%s，我可以帮您解答关于平台使用的问题。\n\n" +
        "**您可以试试这些问题**：\n" +
        "- 如何购买商品？\n" +
        "- 订单流程是怎样的？\n" +
        "- 平台规则是什么？\n\n" +
        "如果您有其他问题，请随时告诉我！",
        roleDesc
    );
  }
  
  /**
   * 调用真实 AI API
   */
  private String callRealAI(String message, String role, List<String> references) {
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

    // 调用智谱 AI API
    try {
      String response = restClient.post()
          .uri("/chat/completions")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
          .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .body(requestBody.toString())
          .retrieve()
          .body(String.class);
      return extractAssistantResponse(response);
    } catch (RestClientException e) {
      logger.error("AI API call failed, falling back to mock response: {}", e.getMessage(), e);
      return getMockResponse(message, role);
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
        return content.asText("抱歉，我无法回答这个问题。");
      }
      return "抱歉，我无法回答这个问题。";
    } catch (Exception e) {
      return "抱歉，我无法回答这个问题。";
    }
  }
}

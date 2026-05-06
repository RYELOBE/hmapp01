package com.campus.trade.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.campus.trade.entity.Product;
import com.campus.trade.entity.User;
import com.campus.trade.mapper.ProductMapper;
import com.campus.trade.mapper.UserMapper;
import com.campus.trade.utils.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    @Value("${campus.ai.api-key:}")
    private String apiKey;

    @Value("${campus.ai.api-url:}")
    private String apiUrl;

    private static final String AI_CACHE_PREFIX = "ai:chat:";

    public Result<Map<String, Object>> chat(Long userId, String message) {
        Map<String, Object> response = new HashMap<>();
        
        if (userId != null) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                response.put("user", user.getUsername());
            }
        }

        String lowerMessage = message.toLowerCase();
        
        if (containsAny(lowerMessage, "推荐", "有什么", "给我看", "想要")) {
            return handleRecommend(userId, message, response);
        } else if (containsAny(lowerMessage, "商品", "产品", "东西", "有没有", "还有吗")) {
            return handleProductQuery(message, response);
        } else if (containsAny(lowerMessage, "发布", "卖", "上架", "开店")) {
            return handlePublishGuide(response);
        } else if (containsAny(lowerMessage, "购买", "买", "下单", "支付")) {
            return handleBuyGuide(response);
        } else if (containsAny(lowerMessage, "你好", "hello", "hi", "在吗", "帮忙")) {
            return handleGreeting(userId, response);
        } else if (containsAny(lowerMessage, "信用", "分数", "认证", "实名")) {
            return handleCreditInfo(response);
        } else if (containsAny(lowerMessage, "安全", "骗", "风险", "保障")) {
            return handleSafetyInfo(response);
        } else {
            return handleDefault(message, response);
        }
    }

    private Result<Map<String, Object>> handleRecommend(Long userId, String message, Map<String, Object> response) {
        String category = detectCategory(message);
        
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        
        if (!category.isEmpty()) {
            wrapper.like(Product::getTitle, category);
        }
        
        wrapper.orderByDesc(Product::getViewCount);
        wrapper.last("LIMIT 6");
        
        List<Product> products = productMapper.selectList(wrapper);
        
        List<Map<String, Object>> productList = new ArrayList<>();
        for (Product product : products) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", product.getId());
            item.put("title", product.getTitle());
            item.put("price", product.getPrice());
            item.put("condition", product.getCondition());
            
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                try {
                    JSONArray images = JSON.parseArray(product.getImages());
                    if (images.size() > 0) {
                        item.put("image", images.getString(0));
                    }
                } catch (Exception e) {
                    item.put("image", "");
                }
            }
            
            productList.add(item);
        }
        
        response.put("type", "recommend");
        response.put("products", productList);
        response.put("message", "根据您的需求，为您推荐以下商品：");
        
        return Result.success(response);
    }

    private Result<Map<String, Object>> handleProductQuery(String message, Map<String, Object> response) {
        response.put("type", "info");
        response.put("message", "您可以告诉我您想要的商品类型，比如：\n" +
                "• \"推荐一些电子产品\"\n" +
                "• \"有没有二手教材\"\n" +
                "• \"给我看看生活用品\"\n\n" +
                "我会帮您查找相关商品并推荐给您！");
        
        return Result.success(response);
    }

    private Result<Map<String, Object>> handlePublishGuide(Map<String, Object> response) {
        response.put("type", "guide");
        response.put("message", "📝 发布商品很简单！\n\n" +
                "发布步骤：\n" +
                "1. 点击 \"发布商品\" 按钮\n" +
                "2. 上传商品图片（最多9张）\n" +
                "3. 填写商品标题和描述\n" +
                "4. 选择分类和成色\n" +
                "5. 设置合理的价格\n" +
                "6. 选择交易方式（自提/邮寄）\n" +
                "7. 提交审核即可\n\n" +
                "💡 小提示：\n" +
                "• 图片清晰能提高成交率\n" +
                "• 如实描述成色更易获得好评\n" +
                "• 价格合理能更快卖出");
        
        return Result.success(response);
    }

    private Result<Map<String, Object>> handleBuyGuide(Map<String, Object> response) {
        response.put("type", "guide");
        response.put("message", "🛒 购买流程：\n\n" +
                "1. 浏览商品，找到喜欢的\n" +
                "2. 点击商品详情查看\n" +
                "3. 可以加入购物车或立即购买\n" +
                "4. 确认收货地址\n" +
                "5. 选择支付方式完成支付\n" +
                "6. 等待卖家发货\n" +
                "7. 收到货后确认收货\n\n" +
                "✅ 交易保障：\n" +
                "• 资金托管，安全无忧\n" +
                "• 校园认证，身份真实\n" +
                "• 信用评分，交易更放心");
        
        return Result.success(response);
    }

    private Result<Map<String, Object>> handleGreeting(Long userId, Map<String, Object> response) {
        String greeting = "👋 你好！我是校园二手平台的AI助手。\n\n" +
                "我可以帮您：\n" +
                "🔍 查找和推荐商品\n" +
                "📝 解答交易问题\n" +
                "💡 提供交易建议\n" +
                "❓ 解答平台使用疑问\n\n" +
                "请告诉我您想做什么？";
        
        if (userId != null) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                greeting = "👋 你好，" + user.getUsername() + "！我是校园二手平台的AI助手。\n\n" +
                        "我可以帮您：\n" +
                        "🔍 查找和推荐商品\n" +
                        "📝 解答交易问题\n" +
                        "💡 提供交易建议\n" +
                        "❓ 解答平台使用疑问\n\n" +
                        "请告诉我您想做什么？";
            }
        }
        
        response.put("type", "greeting");
        response.put("message", greeting);
        
        return Result.success(response);
    }

    private Result<Map<String, Object>> handleCreditInfo(Map<String, Object> response) {
        response.put("type", "info");
        response.put("message", "⭐ 校园信用体系：\n\n" +
                "信用分范围：0-100分\n" +
                "• 0-40分：信用新星\n" +
                "• 41-60分：信用良好\n" +
                "• 61-80分：信用优秀\n" +
                "• 81-90分：信用卓越\n" +
                "• 91-100分：校园达人\n\n" +
                "提升信用分的方法：\n" +
                "✅ 完成校园认证（+10分）\n" +
                "✅ 完成多次交易（+20分）\n" +
                "✅ 获得好评（+10分）\n" +
                "✅ 快速响应（+5分）\n" +
                "❌ 违规会被扣分");
        
        return Result.success(response);
    }

    private Result<Map<String, Object>> handleSafetyInfo(Map<String, Object> response) {
        response.put("type", "info");
        response.put("message", "🔒 交易安全保障：\n\n" +
                "1. 资金托管\n" +
                "   买家付款后，资金由平台保管\n" +
                "   确认收货后才打款给卖家\n\n" +
                "2. 校园认证\n" +
                "   所有用户需完成校园身份认证\n" +
                "   学号和学校信息真实可查\n\n" +
                "3. 信用体系\n" +
                "   每个用户都有信用评分\n" +
                "   查看卖家信用后再交易\n\n" +
                "4. 纠纷调解\n" +
                "   如有问题可申请平台调解\n" +
                "   公正处理各类交易纠纷\n\n" +
                "💡 温馨提示：\n" +
                "• 选择同校交易更安全\n" +
                "• 不要私下转账\n" +
                "• 保留聊天记录");
        
        return Result.success(response);
    }

    private Result<Map<String, Object>> handleDefault(String message, Map<String, Object> response) {
        response.put("type", "default");
        response.put("message", "🤔 我理解您的意思了。\n\n" +
                "我可以帮您：\n" +
                "🔍 搜索和推荐商品\n" +
                "💡 解答各类问题\n" +
                "📝 提供交易指导\n\n" +
                "试试这样问我：\n" +
                "• \"推荐一些电子产品\"\n" +
                "• \"如何发布商品？\"\n" +
                "• \"信用分怎么提升？\"\n" +
                "• \"交易有什么保障？\"");
        
        return Result.success(response);
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String detectCategory(String message) {
        String lowerMessage = message.toLowerCase();
        
        if (containsAny(lowerMessage, "电子", "手机", "电脑", "平板", "电脑")) {
            return "电子";
        } else if (containsAny(lowerMessage, "书籍", "教材", "书", "课本")) {
            return "书籍";
        } else if (containsAny(lowerMessage, "生活", "日用品", "家具")) {
            return "生活";
        } else if (containsAny(lowerMessage, "服饰", "衣服", "鞋子", "包")) {
            return "服饰";
        } else if (containsAny(lowerMessage, "运动", "健身", "体育")) {
            return "运动";
        }
        
        return "";
    }
}

package com.campus.trade.controller;

import com.campus.trade.entity.User;
import com.campus.trade.mapper.UserMapper;
import com.campus.trade.service.impl.AIService;
import com.campus.trade.utils.JwtUtil;
import com.campus.trade.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;
    private final UserMapper userMapper;

    @PostMapping("/chat")
    @Operation(summary = "AI对话")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        
        Long userId = null;
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String token = authentication.getCredentials().toString();
                userId = JwtUtil.getUserIdFromToken(token);
            }
        } catch (Exception e) {
            // user not logged in
        }
        
        return aiService.chat(userId, message);
    }

    @GetMapping("/recommend")
    @Operation(summary = "智能推荐商品")
    public Result<Map<String, Object>> recommend(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "6") Integer limit) {
        
        Long userId = null;
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String token = authentication.getCredentials().toString();
                userId = JwtUtil.getUserIdFromToken(token);
            }
        } catch (Exception e) {
            // user not logged in
        }
        
        return aiService.chat(userId, "推荐" + (category != null ? category : "") + "商品");
    }
}

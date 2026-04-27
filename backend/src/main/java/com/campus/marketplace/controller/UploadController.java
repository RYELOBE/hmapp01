package com.campus.marketplace.controller;

import com.campus.marketplace.service.UploadService;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("/upload")
    public Map<String, Object> upload(MultipartFile file) {
        // 获取当前登录用户 ID（未登录会抛异常，由 GlobalExceptionHandler 处理）
        Long uploaderId = StpUtil.isLogin() ? Long.valueOf(StpUtil.getLoginId().toString()) : null;

        Map<String, Object> result = uploadService.upload(file, uploaderId);

        // 统一返回格式：{ code: 0, data: { url: "..." } }
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("data", result);
        return response;
    }
}

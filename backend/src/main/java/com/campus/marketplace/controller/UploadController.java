package com.campus.marketplace.controller;

import com.campus.marketplace.service.CurrentUserService;
import com.campus.marketplace.service.UploadService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@PreAuthorize("isAuthenticated()")
public class UploadController {

    private final UploadService uploadService;
    private final CurrentUserService currentUserService;

    public UploadController(UploadService uploadService, CurrentUserService currentUserService) {
        this.uploadService = uploadService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/upload")
    public Map<String, Object> upload(MultipartFile file) {
        // 获取当前登录用户 ID（使用 Spring Security）
        Long uploaderId = currentUserService.userId();

        Map<String, Object> result = uploadService.upload(file, uploaderId);

        // 统一返回格式：{ code: 0, data: { url: "..." } }
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", result);
        return response;
    }
}

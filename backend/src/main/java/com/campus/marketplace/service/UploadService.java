package com.campus.marketplace.service;

import com.campus.marketplace.repository.UploadRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UploadService {

    private final UploadRepository uploadRepository;

    // 上传文件保存目录（相对项目根目录）
    private static final String UPLOAD_DIR = "uploads";

    public UploadService(UploadRepository uploadRepository) {
        this.uploadRepository = uploadRepository;
    }

    public Map<String, Object> upload(MultipartFile file, Long uploaderId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 生成唯一文件名
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String storedName = UUID.randomUUID().toString().replace("-", "") + ext;

        // 确保上传目录存在
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件到本地
        File dest = new File(dir, storedName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败：" + e.getMessage());
        }

        // 构建访问 URL
        String url = "/uploads/" + storedName;

        // 保存记录到数据库
        Map<String, Object> record = uploadRepository.save(
                originalName != null ? originalName : "",
                storedName,
                dest.getAbsolutePath(),
                file.getSize(),
                file.getContentType(),
                url,
                uploaderId
        );

        // 返回结果（包含 url）
        Map<String, Object> result = new HashMap<>();
        result.put("url", url);
        result.put("originalName", originalName);
        result.put("fileSize", file.getSize());
        return result;
    }
}

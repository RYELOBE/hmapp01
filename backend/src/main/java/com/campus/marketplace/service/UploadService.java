package com.campus.marketplace.service;

import com.campus.marketplace.repository.UploadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class UploadService {

    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);
    private final UploadRepository uploadRepository;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp");

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    public UploadService(UploadRepository uploadRepository) {
        this.uploadRepository = uploadRepository;
    }

    public Map<String, Object> upload(MultipartFile file, Long uploaderId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("仅支持 JPG/PNG/GIF/WebP/BMP 格式的图片");
        }

        try {
            byte[] bytes = file.getBytes();
            String base64Data = Base64.getEncoder().encodeToString(bytes);

            String originalName = file.getOriginalFilename();
            String storedName = UUID.randomUUID().toString().replace("-", "") + getFileExtension(originalName);

            String dataUrl = String.format("data:%s;base64,%s", contentType, base64Data);

            Map<String, Object> record = uploadRepository.save(
                    originalName != null ? originalName : "",
                    storedName,
                    null,
                    file.getSize(),
                    contentType,
                    dataUrl,
                    base64Data,
                    uploaderId);

            Map<String, Object> result = new HashMap<>();
            result.put("url", dataUrl);
            result.put("originalName", originalName);
            result.put("fileSize", file.getSize());
            result.put("id", record.get("id"));

            logger.info("✅ 文件上传成功: {} ({} KB)", storedName, file.getSize() / 1024);
            return result;

        } catch (Exception e) {
            logger.error("❌ 文件上传失败: {}", e.getMessage());
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains("."))
            return ".png";
        return filename.substring(filename.lastIndexOf("."));
    }
}

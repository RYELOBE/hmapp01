package com.campus.marketplace.service;

import com.campus.marketplace.repository.UploadRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class UploadService {

    private final UploadRepository uploadRepository;
    private final String uploadDir;

    /** 允许上传的图片类型 */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    );

    /** 单个文件最大 5MB */
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    public UploadService(UploadRepository uploadRepository,
                        @Value("${app.upload-dir:./uploads}") String uploadDir) {
        this.uploadRepository = uploadRepository;
        this.uploadDir = uploadDir;
    }

    @PostConstruct
    public void init() {
        Path path = Paths.get(uploadDir);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException("无法创建上传目录: " + uploadDir, e);
            }
        }
    }

    public Map<String, Object> upload(MultipartFile file, Long uploaderId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 文件大小校验
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过 5MB");
        }

        // 文件类型校验
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("仅支持 JPG/PNG/GIF/WebP/BMP 格式的图片");
        }

        // 生成唯一文件名
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String storedName = UUID.randomUUID().toString().replace("-", "") + ext;

        // 保存文件到本地
        File dest = new File(uploadDir, storedName);
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

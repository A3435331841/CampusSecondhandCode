package com.campus.secondhand.controller;

import com.campus.secondhand.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp");

    private static final Set<String> ALLOWED_MIME_TYPES =
            Set.of("image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp");

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * 图片上传接口
     * 返回可访问的图片URL，前端将此URL存入商品的 images 字段
     */
    @PostMapping("/upload")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 文件大小检查
            if (file.getSize() > MAX_FILE_SIZE) {
                return Result.error("图片大小不能超过10MB");
            }

            // MIME type 验证
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
                return Result.error("仅支持上传 JPG、PNG、GIF、WebP、BMP 格式的图片");
            }

            // 扩展名验证
            String originalFilename = file.getOriginalFilename();
            String suffix = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase()
                    : "";
            if (!ALLOWED_EXTENSIONS.contains(suffix)) {
                return Result.error("仅支持上传 JPG、PNG、GIF、WebP、BMP 格式的图片");
            }

            // 解析为绝对路径并确保目录存在
            Path uploadPath = Path.of(uploadDir).toAbsolutePath();
            Files.createDirectories(uploadPath);

            String filename = UUID.randomUUID().toString().replace("-", "") + suffix;
            Path targetFile = uploadPath.resolve(filename);
            file.transferTo(targetFile.toFile());

            // 返回相对访问路径，前端拼接完整 URL
            String url = "/api/file/img/" + filename;
            return Result.success(url);
        } catch (Exception e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }
}

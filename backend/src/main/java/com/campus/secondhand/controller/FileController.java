package com.campus.secondhand.controller;

import com.campus.secondhand.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;

    /**
     * 图片上传接口
     * 返回可访问的图片URL，前端将此URL存入商品的 images 字段
     */
    @PostMapping("/upload")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String filename = UUID.randomUUID().toString().replace("-", "") + suffix;

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            file.transferTo(new File(dir, filename));
        } catch (IOException e) {
            return Result.error("上传失败：" + e.getMessage());
        }

        // 返回相对访问路径，前端拼接完整 URL
        String url = "/api/file/img/" + filename;
        return Result.success(url);
    }
}

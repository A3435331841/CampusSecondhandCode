package com.campus.secondhand.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.ProductCommentDTO;
import com.campus.secondhand.entity.ProductComment;
import com.campus.secondhand.service.ProductCommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private ProductCommentService productCommentService;

    @PostMapping("/add")
    public Result<String> addComment(@RequestBody ProductCommentDTO dto, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        productCommentService.addComment(currentUserId, dto.getProductId(), dto.getContent());
        return Result.success("留言成功");
    }

    @GetMapping("/list")
    public Result<List<ProductComment>> listComments(@RequestParam Long productId) {
        return Result.success(productCommentService.listComments(productId));
    }

    @GetMapping("/admin/list")
    public Result<IPage<ProductComment>> listAllComments(@RequestParam(defaultValue = "1") Integer current,
                                                          @RequestParam(defaultValue = "10") Integer size,
                                                          HttpServletRequest request) {
        if (!"admin".equals(request.getAttribute("currentUserRole"))) {
            return Result.error("无权限操作");
        }
        return Result.success(productCommentService.listAllComments(current, size));
    }

    @PostMapping("/updateStatus")
    public Result<String> updateStatus(@RequestParam Long id,
                                       @RequestParam Integer status,
                                       HttpServletRequest request) {
        if (!"admin".equals(request.getAttribute("currentUserRole"))) {
            return Result.error("无权限操作");
        }
        productCommentService.updateStatus(id, status);
        return Result.success("已更新");
    }
}

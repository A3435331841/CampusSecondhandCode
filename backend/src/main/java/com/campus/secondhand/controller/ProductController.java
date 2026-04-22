package com.campus.secondhand.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.ProductPublishDTO;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 分页查询商品列表（游客可访问，支持关键词搜索和分类筛选）
     */
    @GetMapping("/list")
    public Result<IPage<Product>> listProducts(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId) {
        return Result.success(productService.listProducts(current, size, status, keyword, categoryId));
    }

    /**
     * 获取商品详情（游客可访问）
     */
    @GetMapping("/detail/{id}")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        Product product = productService.getProductDetail(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success(product);
    }

    /**
     * 发布商品（需登录）
     */
    @PostMapping("/publish")
    public Result<String> publishProduct(@RequestBody ProductPublishDTO dto, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        productService.publishProduct(dto, currentUserId);
        return Result.success("商品发布成功，等待审核");
    }

    /**
     * 审核/上下架商品（需管理员）
     */
    @PostMapping("/audit")
    public Result<String> auditProduct(@RequestParam Long id, @RequestParam Integer status,
                                       HttpServletRequest request) {
        String role = (String) request.getAttribute("currentUserRole");
        if (!"admin".equals(role)) {
            return Result.error("无权限操作");
        }
        productService.auditProduct(id, status);
        return Result.success("操作成功");
    }

    /**
     * 查询当前登录用户发布的商品
     */
    @GetMapping("/my")
    public Result<IPage<Product>> getMyProducts(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        return Result.success(productService.getMyProducts(currentUserId, current, size));
    }
}

package com.campus.secondhand.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.ProductPublishDTO;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductMapper productMapper;

    // 分页查询商品 (后台管理/前端首页通用)
    @GetMapping("/list")
    public Result<IPage<Product>> listProducts(@RequestParam(defaultValue = "1") Integer current,
                                               @RequestParam(defaultValue = "10") Integer size,
                                               @RequestParam(required = false) Integer status) {
        Page<Product> page = new Page<>(current, size);
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("create_time");
        IPage<Product> productPage = productMapper.selectPage(page, queryWrapper);
        return Result.success(productPage);
    }

    // 获取单个商品详情
    @GetMapping("/detail/{id}")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success(product);
    }

    // 商品发布接口
    @PostMapping("/publish")
    public Result<String> publishProduct(@RequestBody ProductPublishDTO dto) {
        Product product = new Product();
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategoryId(dto.getCategoryId());
        product.setStock(dto.getStock());
        product.setStatus(0); // 默认为待审核状态
        product.setCreateTime(LocalDateTime.now());
        
        // 模拟当前登录用户ID
        product.setSellerId(1001L); 
        
        productMapper.insert(product);
        return Result.success("商品发布成功，等待审核");
    }

    // 审核/上下架商品接口
    @PostMapping("/audit")
    public Result<String> auditProduct(@RequestParam Long id, @RequestParam Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        productMapper.updateById(product);
        return Result.success("操作成功");
    }
}
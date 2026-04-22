package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.dto.ProductPublishDTO;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public IPage<Product> listProducts(Integer current, Integer size, Integer status, String keyword, Integer categoryId) {
        Page<Product> page = new Page<>(current, size);
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        if (status != null) {
            wrapper.eq("status", status);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like("title", keyword.trim());
        }
        if (categoryId != null) {
            wrapper.eq("category_id", categoryId);
        }
        wrapper.orderByDesc("create_time");
        return productMapper.selectPage(page, wrapper);
    }

    @Override
    public Product getProductDetail(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public void publishProduct(ProductPublishDTO dto, Long sellerId) {
        Product product = new Product();
        product.setSellerId(sellerId);
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setImages(dto.getImages());
        product.setPrice(dto.getPrice());
        product.setCategoryId(dto.getCategoryId());
        product.setStock(dto.getStock() != null ? dto.getStock() : 1);
        product.setStatus(0);
        product.setCreateTime(LocalDateTime.now());
        productMapper.insert(product);
    }

    @Override
    public void auditProduct(Long id, Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        productMapper.updateById(product);
    }

    @Override
    public IPage<Product> getMyProducts(Long sellerId, Integer current, Integer size) {
        Page<Product> page = new Page<>(current, size);
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("seller_id", sellerId).orderByDesc("create_time");
        return productMapper.selectPage(page, wrapper);
    }
}

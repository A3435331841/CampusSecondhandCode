package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.dto.ProductPublishDTO;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.entity.UserBehavior;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.UserBehaviorMapper;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserBehaviorMapper userBehaviorMapper;

    @Override
    public IPage<Product> listProducts(Integer current, Integer size, Integer status, String keyword, Integer categoryId) {
        Page<Product> page = new Page<>(current, size);
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        if (status != null) {
            wrapper.eq("status", status);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String trimmedKeyword = keyword.trim();
            // 使用全文索引搜索 title，同时 LIKE 搜索 description
            wrapper.and(w -> w
                    .apply("MATCH(title) AGAINST({0} IN BOOLEAN MODE)", "+" + trimmedKeyword + "*")
                    .or()
                    .like("description", trimmedKeyword)
            );
        }
        if (categoryId != null) {
            wrapper.eq("category_id", categoryId);
        }
        wrapper.orderByDesc("create_time");

        IPage<Product> result = productMapper.selectPage(page, wrapper);

        // Fallback: 如果全文索引搜索无结果，退回到 LIKE 模糊匹配
        if (keyword != null && !keyword.trim().isEmpty() && result.getRecords().isEmpty()) {
            String trimmedKeyword = keyword.trim();
            QueryWrapper<Product> fallbackWrapper = new QueryWrapper<>();
            if (status != null) {
                fallbackWrapper.eq("status", status);
            }
            fallbackWrapper.and(w -> w.like("title", trimmedKeyword).or().like("description", trimmedKeyword));
            if (categoryId != null) {
                fallbackWrapper.eq("category_id", categoryId);
            }
            fallbackWrapper.orderByDesc("create_time");
            result = productMapper.selectPage(page, fallbackWrapper);
        }

        return result;
    }

    @Override
    public Product getProductDetail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return null;
        }

        UpdateWrapper<Product> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).setSql("view_count = IFNULL(view_count, 0) + 1");
        productMapper.update(null, updateWrapper);

        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(0L);
        behavior.setProductId(id);
        behavior.setBehaviorType("VIEW");
        behavior.setCreateTime(LocalDateTime.now());
        userBehaviorMapper.insert(behavior);

        product.setViewCount((product.getViewCount() == null ? 0 : product.getViewCount()) + 1);
        return product;
    }

    @Override
    public void publishProduct(ProductPublishDTO dto, Long sellerId) {
        User seller = userMapper.selectById(sellerId);
        if (seller == null || !"VERIFIED".equals(seller.getVerifyStatus())) {
            throw new IllegalStateException("请先完成学生认证");
        }

        Product product = new Product();
        product.setSellerId(sellerId);
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setImages(dto.getImages());
        product.setPrice(dto.getPrice());
        product.setCategoryId(dto.getCategoryId());
        product.setStock(dto.getStock() != null ? dto.getStock() : 1);
        product.setPickupPlaceName(dto.getPickupPlaceName());
        product.setPickupAddress(dto.getPickupAddress());
        product.setPickupLat(dto.getPickupLat());
        product.setPickupLng(dto.getPickupLng());
        product.setStatus(0);
        product.setCreateTime(LocalDateTime.now());
        productMapper.insert(product);
    }

    @Override
    public void updateProduct(Long id, ProductPublishDTO dto, Long sellerId) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!product.getSellerId().equals(sellerId)) {
            throw new RuntimeException("无权编辑该商品");
        }
        if (product.getStatus() != 1 && product.getStatus() != 2) {
            throw new RuntimeException("仅在售或下架商品可以编辑");
        }
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setImages(dto.getImages());
        product.setPrice(dto.getPrice());
        product.setCategoryId(dto.getCategoryId());
        product.setPickupPlaceName(dto.getPickupPlaceName());
        product.setPickupAddress(dto.getPickupAddress());
        product.setPickupLat(dto.getPickupLat());
        product.setPickupLng(dto.getPickupLng());
        productMapper.updateById(product);
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

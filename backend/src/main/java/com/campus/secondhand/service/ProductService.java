package com.campus.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.secondhand.dto.ProductPublishDTO;
import com.campus.secondhand.entity.Product;

public interface ProductService {

    IPage<Product> listProducts(Integer current, Integer size, Integer status, String keyword, Integer categoryId);

    Product getProductDetail(Long id);

    void publishProduct(ProductPublishDTO dto, Long sellerId);

    void auditProduct(Long id, Integer status);

    IPage<Product> getMyProducts(Long sellerId, Integer current, Integer size);
}

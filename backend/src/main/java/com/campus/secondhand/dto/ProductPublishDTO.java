package com.campus.secondhand.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductPublishDTO {
    private String title;
    private String description;
    private BigDecimal price;
    private Integer categoryId;
    private Integer stock = 1; // 二手商品默认库存1
}
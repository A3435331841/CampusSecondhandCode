package com.campus.secondhand.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductPublishDTO {
    private String title;
    private String description;
    /** 商品图片URL，多张以逗号分隔 */
    private String images;
    private BigDecimal price;
    private Integer categoryId;
    private Integer stock = 1;
}
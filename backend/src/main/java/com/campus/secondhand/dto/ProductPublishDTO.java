package com.campus.secondhand.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductPublishDTO {
    private String title;
    private String description;
    private String images;
    private BigDecimal price;
    private Integer categoryId;
    private Integer stock = 1;
    private String pickupPlaceName;
    private String pickupAddress;
    private BigDecimal pickupLat;
    private BigDecimal pickupLng;
}

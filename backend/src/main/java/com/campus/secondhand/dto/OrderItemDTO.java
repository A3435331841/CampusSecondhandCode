package com.campus.secondhand.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderItemDTO {
    private String orderNo;
    private Long productId;
    private String productTitle;
    private String productImage;
    private BigDecimal totalAmount;
    private Integer buyCount;
    private Integer status;
    private Integer buyerRated;
    private Integer sellerRated;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
}

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
    /** 0待交接, 1已完成, 2已取消 */
    private Integer status;
    private LocalDateTime createTime;
}

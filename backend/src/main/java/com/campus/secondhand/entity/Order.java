package com.campus.secondhand.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_order")
public class Order {
    @TableId
    private String orderNo;
    private Long buyerId;
    private Long sellerId;
    private Long productId;
    private Integer buyCount;
    private BigDecimal totalAmount;
    private String pickupPlaceName;
    private String pickupAddress;
    private BigDecimal pickupLat;
    private BigDecimal pickupLng;
    private LocalDateTime finishTime;
    private Integer buyerRated;
    private Integer sellerRated;
    private Integer status;
    private LocalDateTime createTime;
}

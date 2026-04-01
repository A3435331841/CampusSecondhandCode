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
    private Long productId;
    private BigDecimal totalAmount;
    private Integer status; // 0待支付, 1已支付, 2已取消
    private LocalDateTime createTime;
}
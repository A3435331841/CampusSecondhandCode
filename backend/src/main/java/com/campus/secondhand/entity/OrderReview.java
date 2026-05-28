package com.campus.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_order_review")
public class OrderReview {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long reviewerId;
    private Long revieweeId;
    private String roleType;
    private Integer score;
    private String content;
    private LocalDateTime createTime;
}

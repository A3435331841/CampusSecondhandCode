package com.campus.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sellerId;
    private Integer categoryId;
    private String title;
    private String description;
    /** 商品图片URL，多张以逗号分隔 */
    private String images;
    private BigDecimal price;
    private Integer stock;
    /** 0待审核, 1在售, 2下架, 3售出 */
    private Integer status;
    private LocalDateTime createTime;
}

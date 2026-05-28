package com.campus.secondhand.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_category")
public class Category {
    @TableId
    private Integer id;
    private String name;
    private Integer sortNo;
    private Integer status;
    private LocalDateTime createTime;
}

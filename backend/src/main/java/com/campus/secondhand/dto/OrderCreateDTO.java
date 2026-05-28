package com.campus.secondhand.dto;

import lombok.Data;

@Data
public class OrderCreateDTO {
    private Long productId;
    private Integer buyCount;
    private String address;
    private String remark;
}
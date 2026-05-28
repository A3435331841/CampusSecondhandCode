package com.campus.secondhand.dto;

import lombok.Data;

@Data
public class ProductReviewDTO {
    private String orderNo;
    private Integer score;
    private String content;
}

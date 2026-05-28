package com.campus.secondhand.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LocationSearchDTO {
    private String keyword;
    private BigDecimal lat;
    private BigDecimal lng;
}

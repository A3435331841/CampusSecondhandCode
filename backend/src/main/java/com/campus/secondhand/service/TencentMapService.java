package com.campus.secondhand.service;

import com.campus.secondhand.dto.LocationSearchDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TencentMapService {
    Map<String, Object> reverseGeocode(BigDecimal lat, BigDecimal lng);

    List<Map<String, Object>> searchSuggestions(LocationSearchDTO dto);
}

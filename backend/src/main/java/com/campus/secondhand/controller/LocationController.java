package com.campus.secondhand.controller;

import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.LocationSearchDTO;
import com.campus.secondhand.service.TencentMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    @Autowired
    private TencentMapService tencentMapService;

    @GetMapping("/reverse")
    public Result<Map<String, Object>> reverseGeocode(@RequestParam BigDecimal lat,
                                                      @RequestParam BigDecimal lng) {
        try {
            return Result.success(tencentMapService.reverseGeocode(lat, lng));
        } catch (IllegalStateException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @GetMapping("/search")
    public Result<List<Map<String, Object>>> searchSuggestions(@ModelAttribute LocationSearchDTO dto) {
        try {
            return Result.success(tencentMapService.searchSuggestions(dto));
        } catch (IllegalStateException exception) {
            return Result.error(exception.getMessage());
        }
    }
}

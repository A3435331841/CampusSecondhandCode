package com.campus.secondhand.service;

import com.campus.secondhand.entity.Product;

import java.util.List;
import java.util.Map;

public interface AiRecommendService {
    List<Product> recommendByDescription(String description);

    Map<String, Object> parseIntent(String description);

    String chat(List<Map<String, String>> history);
}

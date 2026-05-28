package com.campus.secondhand.service;

import com.campus.secondhand.entity.Product;

import java.util.List;

public interface RecommendationService {
    List<Product> listRecommendations(Long userId, Integer size);
}

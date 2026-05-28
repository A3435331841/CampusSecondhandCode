package com.campus.secondhand.service;

import com.campus.secondhand.dto.ProductReviewDTO;

public interface ReviewService {
    void createReview(Long reviewerId, ProductReviewDTO dto);
}

package com.campus.secondhand.service;

import com.campus.secondhand.entity.Product;
import java.util.List;

public interface FavoriteService {
    void favorite(Long userId, Long productId);

    void unfavorite(Long userId, Long productId);

    boolean isFavorited(Long userId, Long productId);

    List<Product> listByUserId(Long userId);
}

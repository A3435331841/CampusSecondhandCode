package com.campus.secondhand.service.impl;

import com.campus.secondhand.entity.Product;
import com.campus.secondhand.entity.UserBehavior;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.UserBehaviorMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {

    @Mock
    private UserBehaviorMapper userBehaviorMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    @Test
    void listRecommendations_shouldPreferCategoryAffinityAndFavorites() {
        UserBehavior behavior = new UserBehavior();
        behavior.setProductId(1L);
        behavior.setBehaviorType("FAVORITE");

        Product historyProduct = new Product();
        historyProduct.setId(1L);
        historyProduct.setCategoryId(2);

        Product recommended = new Product();
        recommended.setId(3002L);
        recommended.setCategoryId(2);
        recommended.setFavoriteCount(10);
        recommended.setCommentCount(5);
        recommended.setStatus(1);

        Product fallback = new Product();
        fallback.setId(3003L);
        fallback.setCategoryId(1);
        fallback.setFavoriteCount(1);
        fallback.setCommentCount(0);
        fallback.setStatus(1);

        when(userBehaviorMapper.selectList(any())).thenReturn(List.of(behavior));
        when(productMapper.selectById(1L)).thenReturn(historyProduct);
        when(productMapper.selectList(any())).thenReturn(List.of(fallback, recommended));

        List<Product> products = recommendationService.listRecommendations(1001L, 10);

        assertEquals(Long.valueOf(3002L), products.get(0).getId());
    }
}

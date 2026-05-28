package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.entity.UserBehavior;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.UserBehaviorMapper;
import com.campus.secondhand.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    @Autowired
    private UserBehaviorMapper userBehaviorMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<Product> listRecommendations(Long userId, Integer size) {
        List<UserBehavior> behaviors = userBehaviorMapper.selectList(
                new QueryWrapper<UserBehavior>().eq("user_id", userId)
        );

        Map<Integer, Integer> categoryPreference = new HashMap<>();
        for (UserBehavior behavior : behaviors) {
            if (behavior.getProductId() == null) {
              continue;
            }
            Product product = productMapper.selectById(behavior.getProductId());
            if (product == null || product.getCategoryId() == null) {
                continue;
            }
            categoryPreference.merge(product.getCategoryId(), weight(behavior.getBehaviorType()), Integer::sum);
        }

        List<Product> candidates = new ArrayList<>(productMapper.selectList(new QueryWrapper<Product>().eq("status", 1)));
        candidates.sort(Comparator.comparingInt(product -> -score(product, categoryPreference)));
        int limit = size == null || size <= 0 ? 10 : size;
        return candidates.size() > limit ? candidates.subList(0, limit) : candidates;
    }

    private int weight(String behaviorType) {
        return switch (behaviorType) {
            case "ORDER" -> 5;
            case "FAVORITE" -> 4;
            case "COMMENT" -> 3;
            case "VIEW" -> 1;
            default -> 1;
        };
    }

    private int score(Product product, Map<Integer, Integer> categoryPreference) {
        int categoryScore = categoryPreference.getOrDefault(product.getCategoryId(), 0) * 5;
        int favoriteScore = (product.getFavoriteCount() == null ? 0 : product.getFavoriteCount()) * 4;
        int commentScore = (product.getCommentCount() == null ? 0 : product.getCommentCount()) * 2;
        int viewScore = product.getViewCount() == null ? 0 : product.getViewCount();
        return categoryScore + favoriteScore + commentScore + viewScore;
    }
}

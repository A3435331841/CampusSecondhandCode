package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.campus.secondhand.entity.Favorite;
import com.campus.secondhand.entity.UserBehavior;
import com.campus.secondhand.mapper.FavoriteMapper;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.UserBehaviorMapper;
import com.campus.secondhand.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import com.campus.secondhand.entity.Product;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserBehaviorMapper userBehaviorMapper;

    @Override
    @Transactional
    public void favorite(Long userId, Long productId) {
        Favorite existing = findFavorite(userId, productId);
        if (existing != null) {
            return;
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favorite.setCreateTime(LocalDateTime.now());
        try {
            favoriteMapper.insert(favorite);
        } catch (DuplicateKeyException exception) {
            return;
        }
        recordBehavior(userId, productId, "FAVORITE");
        adjustFavoriteCount(productId, 1);
    }

    @Override
    @Transactional
    public void unfavorite(Long userId, Long productId) {
        Favorite existing = findFavorite(userId, productId);
        if (existing == null) {
            return;
        }
        favoriteMapper.deleteById(existing.getId());
        adjustFavoriteCount(productId, -1);
    }

    @Override
    public boolean isFavorited(Long userId, Long productId) {
        return findFavorite(userId, productId) != null;
    }

    private Favorite findFavorite(Long userId, Long productId) {
        return favoriteMapper.selectOne(
                new QueryWrapper<Favorite>().eq("user_id", userId).eq("product_id", productId)
        );
    }

    private void adjustFavoriteCount(Long productId, int delta) {
        UpdateWrapper<com.campus.secondhand.entity.Product> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", productId);
        if (delta > 0) {
            wrapper.setSql("favorite_count = IFNULL(favorite_count, 0) + 1");
        } else {
            wrapper.setSql("favorite_count = GREATEST(IFNULL(favorite_count, 0) - 1, 0)");
        }
        productMapper.update(null, wrapper);
    }

    @Override
    public List<Product> listByUserId(Long userId) {
        QueryWrapper<Favorite> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).orderByDesc("create_time");
        List<Favorite> favorites = favoriteMapper.selectList(qw);
        if (favorites == null || favorites.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> productIds = favorites.stream().map(Favorite::getProductId).collect(Collectors.toList());
        return productMapper.selectBatchIds(productIds);
    }

    private void recordBehavior(Long userId, Long productId, String behaviorType) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setProductId(productId);
        behavior.setBehaviorType(behaviorType);
        behavior.setCreateTime(LocalDateTime.now());
        userBehaviorMapper.insert(behavior);
    }
}

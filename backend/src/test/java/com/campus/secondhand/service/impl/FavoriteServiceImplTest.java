package com.campus.secondhand.service.impl;

import com.campus.secondhand.entity.Favorite;
import com.campus.secondhand.mapper.FavoriteMapper;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.UserBehaviorMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplTest {

    @Mock
    private FavoriteMapper favoriteMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private UserBehaviorMapper userBehaviorMapper;

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

    @Test
    void favoriteProduct_shouldBeIdempotentPerUserAndProduct() {
        when(favoriteMapper.selectOne(any())).thenReturn(null).thenReturn(new Favorite());

        favoriteService.favorite(1001L, 2001L);
        favoriteService.favorite(1001L, 2001L);

        verify(favoriteMapper).insert(any(Favorite.class));
        verify(productMapper).update(any(), any());
    }

    @Test
    void unfavoriteProduct_shouldDeleteFavoriteAndDecreaseCounter() {
        Favorite favorite = new Favorite();
        favorite.setId(1L);
        when(favoriteMapper.selectOne(any())).thenReturn(favorite);

        favoriteService.unfavorite(1001L, 2001L);

        verify(favoriteMapper).deleteById(1L);
        verify(productMapper).update(any(), any());
    }
}

package com.campus.secondhand.service.impl;

import com.campus.secondhand.dto.ProductReviewDTO;
import com.campus.secondhand.entity.Order;
import com.campus.secondhand.entity.OrderReview;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.OrderMapper;
import com.campus.secondhand.mapper.OrderReviewMapper;
import com.campus.secondhand.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private OrderReviewMapper orderReviewMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Test
    void createBuyerToSellerReview_shouldUpdateSellerCreditAndPreventDuplicates() {
        Order order = new Order();
        order.setOrderNo("ORD1");
        order.setBuyerId(1001L);
        order.setSellerId(2002L);
        order.setStatus(1);
        order.setBuyerRated(0);
        order.setSellerRated(0);

        ProductReviewDTO dto = new ProductReviewDTO();
        dto.setOrderNo("ORD1");
        dto.setScore(5);
        dto.setContent("Great seller");

        User seller = new User();
        seller.setId(2002L);
        seller.setCreditScore(100);
        seller.setSellRatingAvg(new BigDecimal("0.00"));

        when(orderMapper.selectById("ORD1")).thenReturn(order);
        when(orderReviewMapper.selectOne(any())).thenReturn(null).thenReturn(new OrderReview());
        when(orderReviewMapper.selectList(any())).thenReturn(List.of(buildReview("BUYER_TO_SELLER", 5)));
        when(userMapper.selectById(2002L)).thenReturn(seller);

        reviewService.createReview(1001L, dto);

        assertEquals(1, order.getBuyerRated());
        assertEquals(110, seller.getCreditScore());

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> reviewService.createReview(1001L, dto));
        assertEquals("已评价过该订单", exception.getMessage());
    }

    private OrderReview buildReview(String roleType, int score) {
        OrderReview review = new OrderReview();
        review.setRoleType(roleType);
        review.setScore(score);
        review.setCreateTime(LocalDateTime.now());
        return review;
    }
}

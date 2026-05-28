package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.secondhand.dto.ProductReviewDTO;
import com.campus.secondhand.entity.Order;
import com.campus.secondhand.entity.OrderReview;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.OrderMapper;
import com.campus.secondhand.mapper.OrderReviewMapper;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private OrderReviewMapper orderReviewMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public void createReview(Long reviewerId, ProductReviewDTO dto) {
        Order order = orderMapper.selectById(dto.getOrderNo());
        if (order == null) {
            throw new IllegalStateException("订单不存在");
        }
        if (order.getStatus() == null || order.getStatus() != 1) {
            throw new IllegalStateException("订单未完成");
        }

        String roleType;
        Long revieweeId;
        if (reviewerId.equals(order.getBuyerId())) {
            roleType = "BUYER_TO_SELLER";
            revieweeId = order.getSellerId();
        } else if (reviewerId.equals(order.getSellerId())) {
            roleType = "SELLER_TO_BUYER";
            revieweeId = order.getBuyerId();
        } else {
            throw new IllegalStateException("无权评价该订单");
        }

        OrderReview existing = orderReviewMapper.selectOne(
                new QueryWrapper<OrderReview>()
                        .eq("order_no", dto.getOrderNo())
                        .eq("reviewer_id", reviewerId)
                        .eq("role_type", roleType)
        );
        if (existing != null) {
            throw new IllegalStateException("已评价过该订单");
        }

        OrderReview review = new OrderReview();
        review.setOrderNo(dto.getOrderNo());
        review.setReviewerId(reviewerId);
        review.setRevieweeId(revieweeId);
        review.setRoleType(roleType);
        review.setScore(dto.getScore());
        review.setContent(dto.getContent());
        review.setCreateTime(LocalDateTime.now());
        orderReviewMapper.insert(review);

        if ("BUYER_TO_SELLER".equals(roleType)) {
            order.setBuyerRated(1);
        } else {
            order.setSellerRated(1);
        }
        orderMapper.updateById(order);

        updateUserRatingAndCredit(revieweeId, roleType, dto.getScore());
    }

    private void updateUserRatingAndCredit(Long userId, String roleType, Integer score) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }

        List<OrderReview> reviews = orderReviewMapper.selectList(
                new QueryWrapper<OrderReview>()
                        .eq("reviewee_id", userId)
                        .eq("role_type", roleType)
        );

        BigDecimal average = BigDecimal.ZERO;
        if (!reviews.isEmpty()) {
            int total = reviews.stream().map(OrderReview::getScore).reduce(0, Integer::sum);
            average = BigDecimal.valueOf(total)
                    .divide(BigDecimal.valueOf(reviews.size()), 2, RoundingMode.HALF_UP);
        }

        if ("BUYER_TO_SELLER".equals(roleType)) {
            user.setSellRatingAvg(average);
        } else {
            user.setBuyRatingAvg(average);
        }

        int currentCredit = user.getCreditScore() == null ? 100 : user.getCreditScore();
        int nextCredit = Math.max(0, Math.min(200, currentCredit + ((score == null ? 0 : score) - 3) * 5));
        user.setCreditScore(nextCredit);
        userMapper.updateById(user);
    }
}

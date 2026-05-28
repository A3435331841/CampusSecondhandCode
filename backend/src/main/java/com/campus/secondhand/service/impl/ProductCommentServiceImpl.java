package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.entity.ProductComment;
import com.campus.secondhand.entity.UserBehavior;
import com.campus.secondhand.mapper.ProductCommentMapper;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.UserBehaviorMapper;
import com.campus.secondhand.service.ProductCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductCommentServiceImpl implements ProductCommentService {

    @Autowired
    private ProductCommentMapper productCommentMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserBehaviorMapper userBehaviorMapper;

    @Override
    @Transactional
    public void addComment(Long userId, Long productId, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("请输入留言内容");
        }

        ProductComment comment = new ProductComment();
        comment.setUserId(userId);
        comment.setProductId(productId);
        comment.setContent(content.trim());
        comment.setStatus(1);
        comment.setCreateTime(LocalDateTime.now());
        productCommentMapper.insert(comment);
        recordBehavior(userId, productId, "COMMENT");

        UpdateWrapper<Product> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", productId).setSql("comment_count = IFNULL(comment_count, 0) + 1");
        productMapper.update(null, wrapper);
    }

    @Override
    public List<ProductComment> listComments(Long productId) {
        return productCommentMapper.selectList(
                new QueryWrapper<ProductComment>()
                        .eq("product_id", productId)
                        .eq("status", 1)
                        .orderByAsc("create_time")
        );
    }

    @Override
    public IPage<ProductComment> listAllComments(Integer current, Integer size) {
        Page<ProductComment> page = new Page<>(current, size);
        return productCommentMapper.selectPage(page,
                new QueryWrapper<ProductComment>().orderByDesc("create_time"));
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        ProductComment comment = new ProductComment();
        comment.setId(id);
        comment.setStatus(status);
        productCommentMapper.updateById(comment);
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

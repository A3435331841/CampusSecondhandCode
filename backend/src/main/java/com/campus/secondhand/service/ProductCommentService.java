package com.campus.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.secondhand.entity.ProductComment;

import java.util.List;

public interface ProductCommentService {
    void addComment(Long userId, Long productId, String content);

    List<ProductComment> listComments(Long productId);

    IPage<ProductComment> listAllComments(Integer current, Integer size);

    void updateStatus(Long id, Integer status);
}

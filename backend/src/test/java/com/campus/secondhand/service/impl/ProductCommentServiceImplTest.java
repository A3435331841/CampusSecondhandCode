package com.campus.secondhand.service.impl;

import com.campus.secondhand.entity.ProductComment;
import com.campus.secondhand.mapper.ProductCommentMapper;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.UserBehaviorMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCommentServiceImplTest {

    @Mock
    private ProductCommentMapper productCommentMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private UserBehaviorMapper userBehaviorMapper;

    @InjectMocks
    private ProductCommentServiceImpl productCommentService;

    @Test
    void addComment_shouldPersistCommentAndIncreaseProductCount() {
        productCommentService.addComment(1001L, 3001L, "Looks good");

        ArgumentCaptor<ProductComment> captor = ArgumentCaptor.forClass(ProductComment.class);
        verify(productCommentMapper).insert(captor.capture());
        assertEquals(1001L, captor.getValue().getUserId());
        assertEquals(3001L, captor.getValue().getProductId());
        assertEquals("Looks good", captor.getValue().getContent());
        verify(productMapper).update(any(), any());
    }

    @Test
    void listComments_shouldReturnMapperResults() {
        ProductComment comment = new ProductComment();
        comment.setProductId(3001L);
        when(productCommentMapper.selectList(any())).thenReturn(List.of(comment));

        List<ProductComment> comments = productCommentService.listComments(3001L);

        assertEquals(1, comments.size());
        assertEquals(3001L, comments.get(0).getProductId());
    }
}

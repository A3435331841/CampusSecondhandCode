package com.campus.secondhand.service.impl;

import com.campus.secondhand.dto.OrderCreateDTO;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.mapper.OrderMapper;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.UserBehaviorMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserBehaviorMapper userBehaviorMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrder_shouldRejectBuyingOwnProductBeforeDeductingStock() {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setProductId(88L);
        dto.setBuyCount(1);

        Product product = new Product();
        product.setId(88L);
        product.setSellerId(1001L);
        product.setStatus(1);
        product.setStock(2);
        product.setPrice(new BigDecimal("12.00"));
        when(productMapper.selectById(88L)).thenReturn(product);

        RuntimeException exception =
                assertThrows(RuntimeException.class, () -> orderService.createOrder(dto, 1001L));

        assertEquals("不能购买自己发布的商品", exception.getMessage());
        verify(stringRedisTemplate, never()).execute(any(), any(), any());
        verify(orderMapper, never()).insert(any());
    }

    @Test
    void createOrder_shouldRejectUnapprovedProductBeforeDeductingStock() {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setProductId(99L);
        dto.setBuyCount(1);

        Product product = new Product();
        product.setId(99L);
        product.setSellerId(1002L);
        product.setStatus(0);
        product.setStock(3);
        product.setPrice(new BigDecimal("20.00"));
        when(productMapper.selectById(99L)).thenReturn(product);

        RuntimeException exception =
                assertThrows(RuntimeException.class, () -> orderService.createOrder(dto, 1001L));

        assertEquals("商品未上架或不可购买", exception.getMessage());
        verify(stringRedisTemplate, never()).execute(any(), any(), any());
        verify(orderMapper, never()).insert(any());
    }
}

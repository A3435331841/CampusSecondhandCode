package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.dto.ProductPublishDTO;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void publishProduct_shouldMapDtoAndDefaultStock() {
        ProductPublishDTO dto = new ProductPublishDTO();
        dto.setTitle("Java教材");
        dto.setDescription("九成新");
        dto.setImages("http://localhost:8080/api/file/img/a.jpg");
        dto.setPrice(new BigDecimal("35.50"));
        dto.setCategoryId(2);
        dto.setStock(null);

        productService.publishProduct(dto, 1001L);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper).insert(productCaptor.capture());
        Product saved = productCaptor.getValue();
        assertEquals(1001L, saved.getSellerId());
        assertEquals("Java教材", saved.getTitle());
        assertEquals("九成新", saved.getDescription());
        assertEquals("http://localhost:8080/api/file/img/a.jpg", saved.getImages());
        assertEquals(new BigDecimal("35.50"), saved.getPrice());
        assertEquals(2, saved.getCategoryId());
        assertEquals(1, saved.getStock());
        assertEquals(0, saved.getStatus());
        assertNotNull(saved.getCreateTime());
    }

    @Test
    void getMyProducts_shouldUseSellerFilterAndPageParams() {
        when(productMapper.selectPage(any(Page.class), any(QueryWrapper.class)))
                .thenReturn(new Page<>());

        productService.getMyProducts(2002L, 3, 15);

        ArgumentCaptor<Page<Product>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        ArgumentCaptor<QueryWrapper<Product>> wrapperCaptor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(productMapper).selectPage(pageCaptor.capture(), wrapperCaptor.capture());

        Page<Product> page = pageCaptor.getValue();
        assertEquals(3, page.getCurrent());
        assertEquals(15, page.getSize());

        String sqlSegment = wrapperCaptor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("seller_id"));
        assertTrue(sqlSegment.contains("create_time"));
    }
}

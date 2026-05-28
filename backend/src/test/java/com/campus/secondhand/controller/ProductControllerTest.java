package com.campus.secondhand.controller;

import com.campus.secondhand.service.ProductService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProductController controller = new ProductController();
        ReflectionTestUtils.setField(controller, "productService", productService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void publishProduct_shouldReturnJsonErrorWhenUserIsNotVerified() throws Exception {
        doThrow(new IllegalStateException("请先完成学生认证"))
                .when(productService).publishProduct(any(), eq(1001L));

        mockMvc.perform(post("/api/product/publish")
                        .requestAttr("currentUserId", 1001L)
                        .contentType("application/json")
                        .content("""
                                {
                                  "title":"测试商品",
                                  "description":"测试描述",
                                  "images":"http://localhost:8080/api/file/img/a.jpg",
                                  "price":12.5,
                                  "categoryId":1,
                                  "stock":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(500)))
                .andExpect(jsonPath("$.msg", is("请先完成学生认证")));
    }

    @Test
    void listProducts_shouldForcePublicListToOnSaleProducts() throws Exception {
        when(productService.listProducts(1, 10, 1, "电脑", 1)).thenReturn(new Page<Product>());

        mockMvc.perform(get("/api/product/list")
                        .param("current", "1")
                        .param("size", "10")
                        .param("status", "0")
                        .param("keyword", "电脑")
                        .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)));

        verify(productService).listProducts(1, 10, 1, "电脑", 1);
    }

    @Test
    void listAdminProducts_shouldRequireAdminRole() throws Exception {
        mockMvc.perform(get("/api/product/admin/list")
                        .requestAttr("currentUserRole", "user")
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(500)))
                .andExpect(jsonPath("$.msg", is("无权限操作")));
    }

    @Test
    void listAdminProducts_shouldAllowAdminStatusFilter() throws Exception {
        when(productService.listProducts(1, 10, 0, null, null)).thenReturn(new Page<Product>());

        mockMvc.perform(get("/api/product/admin/list")
                        .requestAttr("currentUserRole", "admin")
                        .param("current", "1")
                        .param("size", "10")
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)));

        verify(productService).listProducts(1, 10, 0, null, null);
    }
}

package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.config.TencentMapProperties;
import com.campus.secondhand.config.WechatProperties;
import com.campus.secondhand.dto.ProductPublishDTO;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.UserBehaviorMapper;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    private static final Path APPLICATION_YML =
            Path.of("src", "main", "resources", "application.yml");
    private static final Path DATABASE_SQL =
            Path.of("src", "main", "resources", "database.sql");
    private static final Path DATA_SQL =
            Path.of("src", "main", "resources", "data.sql");

    @Mock
    private ProductMapper productMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserBehaviorMapper userBehaviorMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void publishProduct_shouldMapDtoAndDefaultStock() {
        ProductPublishDTO dto = new ProductPublishDTO();
        dto.setTitle("Java textbook");
        dto.setDescription("Good condition");
        dto.setImages("http://localhost:8080/api/file/img/a.jpg");
        dto.setPrice(new BigDecimal("35.50"));
        dto.setCategoryId(2);
        dto.setStock(null);
        dto.setPickupPlaceName("Library Gate");
        dto.setPickupAddress("Library Road");
        dto.setPickupLat(new BigDecimal("22.500000"));
        dto.setPickupLng(new BigDecimal("113.900000"));

        User seller = new User();
        seller.setId(1001L);
        seller.setVerifyStatus("VERIFIED");
        when(userMapper.selectById(1001L)).thenReturn(seller);

        productService.publishProduct(dto, 1001L);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper).insert(productCaptor.capture());
        Product saved = productCaptor.getValue();
        assertEquals(1001L, saved.getSellerId());
        assertEquals("Java textbook", saved.getTitle());
        assertEquals("Good condition", saved.getDescription());
        assertEquals("http://localhost:8080/api/file/img/a.jpg", saved.getImages());
        assertEquals(new BigDecimal("35.50"), saved.getPrice());
        assertEquals(2, saved.getCategoryId());
        assertEquals(1, saved.getStock());
        assertEquals("Library Gate", saved.getPickupPlaceName());
        assertEquals("Library Road", saved.getPickupAddress());
        assertEquals(new BigDecimal("22.500000"), saved.getPickupLat());
        assertEquals(new BigDecimal("113.900000"), saved.getPickupLng());
        assertEquals(0, saved.getStatus());
        assertNotNull(saved.getCreateTime());
    }

    @Test
    void publishProduct_shouldRequireVerifiedUserBeforeFuturePublishGate() {
        ProductPublishDTO dto = new ProductPublishDTO();
        dto.setTitle("Java textbook");
        dto.setDescription("Good condition");
        dto.setImages("http://localhost:8080/api/file/img/a.jpg");
        dto.setPrice(new BigDecimal("35.50"));
        dto.setCategoryId(2);
        dto.setStock(1);

        User seller = new User();
        seller.setId(1001L);
        seller.setVerifyStatus("UNVERIFIED");
        when(userMapper.selectById(1001L)).thenReturn(seller);

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> productService.publishProduct(dto, 1001L));
        assertEquals("请先完成学生认证", exception.getMessage());
    }

    @Test
    void getMyProducts_shouldPassPageParamsToMapper() {
        when(productMapper.selectPage(any(Page.class), any(QueryWrapper.class)))
                .thenReturn(new Page<>());

        productService.getMyProducts(2002L, 3, 15);

        ArgumentCaptor<Page<Product>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        ArgumentCaptor<QueryWrapper<Product>> wrapperCaptor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(productMapper).selectPage(pageCaptor.capture(), wrapperCaptor.capture());

        Page<Product> page = pageCaptor.getValue();
        assertEquals(3, page.getCurrent());
        assertEquals(15, page.getSize());
        assertNotNull(wrapperCaptor.getValue());
    }

    @Test
    void getProductDetail_shouldNotRecordViewForMissingProduct() {
        when(productMapper.selectById(404L)).thenReturn(null);

        Product product = productService.getProductDetail(404L);

        assertEquals(null, product);
        verify(productMapper, never()).update(any(), any());
        verify(userBehaviorMapper, never()).insert(any());
    }

    @Test
    void configBaseline_shouldUseEnvBackedKeysAndBScopeSchemaMarkers() throws IOException {
        ConfigurationProperties wechatProperties =
                WechatProperties.class.getAnnotation(ConfigurationProperties.class);
        assertNotNull(wechatProperties);
        assertEquals("wechat.mini-app", wechatProperties.prefix());

        ConfigurationProperties tencentMapProperties =
                TencentMapProperties.class.getAnnotation(ConfigurationProperties.class);
        assertNotNull(tencentMapProperties);
        assertEquals("tencent.map", tencentMapProperties.prefix());

        String applicationYml = Files.readString(APPLICATION_YML, StandardCharsets.UTF_8);
        assertTrue(applicationYml.contains("app-id: ${WECHAT_MINI_APP_ID:}"));
        assertTrue(applicationYml.contains("app-secret: ${WECHAT_MINI_APP_SECRET:}"));
        assertTrue(applicationYml.contains("key: ${TENCENT_MAP_KEY:}"));
        assertTrue(applicationYml.contains("${DB_USER:root}"));
        assertTrue(applicationYml.contains("${DB_PASSWORD:}"));

        String databaseSql = Files.readString(DATABASE_SQL, StandardCharsets.UTF_8);
        assertTrue(databaseSql.contains("`verify_status`"));
        assertTrue(databaseSql.contains("`real_name`"));
        assertTrue(databaseSql.contains("`buy_rating_avg`"));
        assertTrue(databaseSql.contains("`sell_rating_avg`"));
        assertTrue(databaseSql.contains("`pickup_place_name`"));
        assertTrue(databaseSql.contains("`pickup_address`"));
        assertTrue(databaseSql.contains("`pickup_lat`"));
        assertTrue(databaseSql.contains("`pickup_lng`"));
        assertTrue(databaseSql.contains("`view_count`"));
        assertTrue(databaseSql.contains("`favorite_count`"));
        assertTrue(databaseSql.contains("`comment_count`"));
        assertTrue(databaseSql.contains("`finish_time`"));
        assertTrue(databaseSql.contains("`buyer_rated`"));
        assertTrue(databaseSql.contains("`seller_rated`"));
        assertTrue(databaseSql.contains("CREATE TABLE IF NOT EXISTS `biz_student_roster`"));
        assertTrue(databaseSql.contains("CREATE TABLE IF NOT EXISTS `biz_favorite`"));
        assertTrue(databaseSql.contains("CREATE TABLE IF NOT EXISTS `biz_product_comment`"));
        assertTrue(databaseSql.contains("CREATE TABLE IF NOT EXISTS `biz_order_review`"));
        assertTrue(databaseSql.contains("CREATE TABLE IF NOT EXISTS `biz_user_behavior`"));
        assertTrue(databaseSql.contains("CREATE TABLE IF NOT EXISTS `biz_category`"));

        String dataSql = Files.readString(DATA_SQL, StandardCharsets.UTF_8);
        assertTrue(dataSql.contains("INSERT INTO `biz_category`"));
        assertTrue(dataSql.contains("(1, '数码电子', 1, 1)"));
        assertTrue(dataSql.contains("(6, '其他闲置', 6, 1)"));
    }
}

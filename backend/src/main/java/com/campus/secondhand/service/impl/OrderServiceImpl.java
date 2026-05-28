package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.dto.OrderCreateDTO;
import com.campus.secondhand.dto.OrderItemDTO;
import com.campus.secondhand.dto.OrderStatsDTO;
import com.campus.secondhand.entity.Order;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.entity.UserBehavior;
import com.campus.secondhand.mapper.OrderMapper;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.UserBehaviorMapper;
import com.campus.secondhand.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private static final String STOCK_DEDUCT_SCRIPT =
            "local stockKey = KEYS[1];" +
            "local deductCount = tonumber(ARGV[1]);" +
            "local currentStock = tonumber(redis.call('GET', stockKey));" +
            "if currentStock == nil then return -1; end;" +
            "if currentStock >= deductCount then " +
            "   redis.call('DECRBY', stockKey, deductCount); " +
            "   return 1; " +
            "else " +
            "   return 0; " +
            "end;";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserBehaviorMapper userBehaviorMapper;

    @Override
    @Transactional
    public String createOrder(OrderCreateDTO dto, Long userId) {
        Long productId = dto.getProductId();
        Integer buyCount = dto.getBuyCount() == null || dto.getBuyCount() <= 0 ? 1 : dto.getBuyCount();

        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (product.getStatus() == null || product.getStatus() != 1) {
            throw new RuntimeException("商品未上架或不可购买");
        }
        if (product.getSellerId() != null && product.getSellerId().equals(userId)) {
            throw new RuntimeException("不能购买自己发布的商品");
        }

        int currentStock = product.getStock() == null ? 0 : product.getStock();
        if (currentStock < buyCount) {
            throw new RuntimeException("商品库存不足");
        }

        String stockKey = "product:stock:" + productId;

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(STOCK_DEDUCT_SCRIPT, Long.class);
        Long result = stringRedisTemplate.execute(redisScript, Collections.singletonList(stockKey), String.valueOf(buyCount));

        if (result == null || result == 0L) {
            throw new RuntimeException("商品库存不足");
        } else if (result == -1L) {
            stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(currentStock));
            Long retry = stringRedisTemplate.execute(redisScript, Collections.singletonList(stockKey), String.valueOf(buyCount));
            if (retry == null || retry != 1L) {
                throw new RuntimeException("商品库存不足");
            }
        }

        product.setStock(currentStock - buyCount);
        if (product.getStock() == 0) {
            product.setStatus(3);
        }
        productMapper.updateById(product);

        Order order = new Order();
        order.setOrderNo("ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        order.setBuyerId(userId);
        order.setSellerId(product.getSellerId());
        order.setProductId(productId);
        order.setBuyCount(buyCount);
        order.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(buyCount)));
        order.setPickupPlaceName(product.getPickupPlaceName());
        order.setPickupAddress(product.getPickupAddress());
        order.setPickupLat(product.getPickupLat());
        order.setPickupLng(product.getPickupLng());
        order.setBuyerRated(0);
        order.setSellerRated(0);
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);
        recordBehavior(userId, productId, "ORDER");

        return order.getOrderNo();
    }

    @Override
    public IPage<OrderItemDTO> listMyBuyOrders(Long userId, Integer current, Integer size) {
        return mapOrders(orderMapper.selectPage(
                new Page<>(current, size),
                new QueryWrapper<Order>().eq("buyer_id", userId).orderByDesc("create_time")
        ), current, size);
    }

    @Override
    public IPage<OrderItemDTO> listMySellOrders(Long userId, Integer current, Integer size) {
        return mapOrders(orderMapper.selectPage(
                new Page<>(current, size),
                new QueryWrapper<Order>().eq("seller_id", userId).orderByDesc("create_time")
        ), current, size);
    }

    @Override
    @Transactional
    public void cancelOrder(String orderNo, Long userId) {
        Order order = orderMapper.selectById(orderNo);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new RuntimeException("无权取消该订单");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("当前订单不可取消");
        }

        order.setStatus(2);
        orderMapper.updateById(order);

        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            int restore = order.getBuyCount() == null ? 1 : order.getBuyCount();
            product.setStock(product.getStock() + restore);
            if (product.getStatus() == 3 && product.getStock() > 0) {
                product.setStatus(1);
            }
            productMapper.updateById(product);
            stringRedisTemplate.opsForValue().set("product:stock:" + order.getProductId(), String.valueOf(product.getStock()));
        }
    }

    @Override
    @Transactional
    public void confirmOrder(String orderNo, Long userId) {
        Order order = orderMapper.selectById(orderNo);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new RuntimeException("无权确认该订单");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("当前订单不可确认");
        }
        order.setStatus(1);
        order.setFinishTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    private IPage<OrderItemDTO> mapOrders(IPage<Order> orderPage, Integer current, Integer size) {
        List<OrderItemDTO> list = new ArrayList<>();
        for (Order order : orderPage.getRecords()) {
            OrderItemDTO dto = new OrderItemDTO();
            dto.setOrderNo(order.getOrderNo());
            dto.setProductId(order.getProductId());
            dto.setTotalAmount(order.getTotalAmount());
            dto.setBuyCount(order.getBuyCount());
            dto.setStatus(order.getStatus());
            dto.setBuyerRated(order.getBuyerRated());
            dto.setSellerRated(order.getSellerRated());
            dto.setCreateTime(order.getCreateTime());
            dto.setFinishTime(order.getFinishTime());
            Product product = productMapper.selectById(order.getProductId());
            if (product != null) {
                dto.setProductTitle(product.getTitle());
                if (product.getImages() != null && !product.getImages().isBlank()) {
                    dto.setProductImage(product.getImages().split(",")[0].trim());
                }
            }
            list.add(dto);
        }

        Page<OrderItemDTO> result = new Page<>(current, size);
        result.setTotal(orderPage.getTotal());
        result.setRecords(list);
        return result;
    }

    @Override
    public OrderStatsDTO getStats() {
        OrderStatsDTO stats = new OrderStatsDTO();
        stats.setTotalOrders(orderMapper.selectCount(null));
        stats.setPendingOrders(orderMapper.selectCount(
                new QueryWrapper<Order>().eq("status", 0)));
        stats.setCompletedOrders(orderMapper.selectCount(
                new QueryWrapper<Order>().eq("status", 1)));
        stats.setCancelledOrders(orderMapper.selectCount(
                new QueryWrapper<Order>().eq("status", 2)));
        return stats;
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

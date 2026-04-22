package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.dto.OrderCreateDTO;
import com.campus.secondhand.dto.OrderItemDTO;
import com.campus.secondhand.entity.Order;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.mapper.OrderMapper;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    // Redis Lua 脚本：原子性检查库存并扣减
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

    @Override
    @Transactional
    public String createOrder(OrderCreateDTO dto, Long userId) {
        Long productId = dto.getProductId();
        Integer buyCount = dto.getBuyCount() == null || dto.getBuyCount() <= 0 ? 1 : dto.getBuyCount();
        String stockKey = "product:stock:" + productId;

        // 1. 执行 Lua 脚本在 Redis 中预扣减库存，防止高并发超卖
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(STOCK_DEDUCT_SCRIPT, Long.class);
        Long result = stringRedisTemplate.execute(redisScript, Collections.singletonList(stockKey), String.valueOf(buyCount));

        if (result == null || result == 0L) {
            throw new RuntimeException("手慢了，商品已被抢购空！");
        } else if (result == -1L) {
            // 如果 Redis 中没有缓存库存，先从数据库加载（缓存击穿兜底策略）
            Product product = productMapper.selectById(productId);
            if (product == null || product.getStock() <= 0) {
                throw new RuntimeException("商品不存在或已售罄！");
            }
            stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(product.getStock()));
            // 重新执行脚本
            Long retryResult = stringRedisTemplate.execute(redisScript, Collections.singletonList(stockKey), String.valueOf(buyCount));
            if (retryResult == null || retryResult != 1L) {
                throw new RuntimeException("手慢了，商品已被抢购空！");
            }
        }

        // 2. 数据库实际扣减库存 (通过乐观锁或直接 Update)
        Product product = productMapper.selectById(productId);
        product.setStock(product.getStock() - buyCount);
        // 如果库存为0，自动下架或标记为售出
        if (product.getStock() == 0) {
            product.setStatus(3); 
        }
        productMapper.updateById(product);

        // 3. 生成订单入库
        Order order = new Order();
        String orderNo = "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        order.setOrderNo(orderNo);
        order.setBuyerId(userId);
        order.setSellerId(product.getSellerId());
        order.setProductId(productId);
        order.setBuyCount(buyCount);
        // 计算总金额
        order.setTotalAmount(product.getPrice().multiply(new java.math.BigDecimal(buyCount)));
        order.setStatus(0); // 0待交接
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);

        // TODO: 这里应该发送一个消息到 RabbitMQ 的延迟队列，15分钟后检查订单是否支付，未支付则取消订单并回滚 Redis 和 MySQL 的库存

        return orderNo;
    }

    @Override
    public IPage<OrderItemDTO> listMyBuyOrders(Long userId, Integer current, Integer size) {
        Page<Order> page = new Page<>(current, size);
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("buyer_id", userId).orderByDesc("create_time");
        IPage<Order> orderPage = orderMapper.selectPage(page, wrapper);

        List<OrderItemDTO> list = new ArrayList<>();
        for (Order order : orderPage.getRecords()) {
            OrderItemDTO dto = new OrderItemDTO();
            dto.setOrderNo(order.getOrderNo());
            dto.setProductId(order.getProductId());
            dto.setTotalAmount(order.getTotalAmount());
            dto.setBuyCount(order.getBuyCount());
            dto.setStatus(order.getStatus());
            dto.setCreateTime(order.getCreateTime());
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
    @Transactional
    public void cancelOrder(String orderNo, Long userId) {
        Order order = orderMapper.selectById(orderNo);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new RuntimeException("无权限取消该订单");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("当前订单状态不可取消");
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
            String stockKey = "product:stock:" + order.getProductId();
            stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(product.getStock()));
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
            throw new RuntimeException("无权限确认该订单");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("当前订单状态不可确认");
        }
        order.setStatus(1);
        orderMapper.updateById(order);
    }
}

package com.campus.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.secondhand.dto.OrderCreateDTO;
import com.campus.secondhand.dto.OrderItemDTO;

public interface OrderService {
    String createOrder(OrderCreateDTO dto, Long userId);

    IPage<OrderItemDTO> listMyBuyOrders(Long userId, Integer current, Integer size);

    void cancelOrder(String orderNo, Long userId);

    void confirmOrder(String orderNo, Long userId);
}

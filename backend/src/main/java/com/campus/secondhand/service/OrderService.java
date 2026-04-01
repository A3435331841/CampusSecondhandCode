package com.campus.secondhand.service;

import com.campus.secondhand.dto.OrderCreateDTO;

public interface OrderService {
    String createOrder(OrderCreateDTO dto, Long userId);
}
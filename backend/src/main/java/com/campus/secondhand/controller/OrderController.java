package com.campus.secondhand.controller;

import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.OrderCreateDTO;
import com.campus.secondhand.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public Result<String> createOrder(@RequestBody OrderCreateDTO dto) {
        try {
            // 模拟当前登录用户的 ID
            Long currentUserId = 1002L;
            String orderNo = orderService.createOrder(dto, currentUserId);
            return Result.success(orderNo);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
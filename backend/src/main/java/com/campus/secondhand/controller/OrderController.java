package com.campus.secondhand.controller;

import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.OrderCreateDTO;
import com.campus.secondhand.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
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
    public Result<String> createOrder(@RequestBody OrderCreateDTO dto, HttpServletRequest request) {
        try {
            Long currentUserId = (Long) request.getAttribute("currentUserId");
            String orderNo = orderService.createOrder(dto, currentUserId);
            return Result.success(orderNo);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
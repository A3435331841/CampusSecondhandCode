package com.campus.secondhand.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.OrderCreateDTO;
import com.campus.secondhand.dto.OrderItemDTO;
import com.campus.secondhand.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/my/buy")
    public Result<IPage<OrderItemDTO>> myBuyOrders(@RequestParam(defaultValue = "1") Integer current,
                                                    @RequestParam(defaultValue = "10") Integer size,
                                                    HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        return Result.success(orderService.listMyBuyOrders(currentUserId, current, size));
    }

    @PostMapping("/cancel")
    public Result<String> cancelOrder(@RequestParam String orderNo, HttpServletRequest request) {
        try {
            Long currentUserId = (Long) request.getAttribute("currentUserId");
            orderService.cancelOrder(orderNo, currentUserId);
            return Result.success("订单已取消");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/confirm")
    public Result<String> confirmOrder(@RequestParam String orderNo, HttpServletRequest request) {
        try {
            Long currentUserId = (Long) request.getAttribute("currentUserId");
            orderService.confirmOrder(orderNo, currentUserId);
            return Result.success("确认收货成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}

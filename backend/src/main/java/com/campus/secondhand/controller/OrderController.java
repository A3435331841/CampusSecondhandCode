package com.campus.secondhand.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.OrderCreateDTO;
import com.campus.secondhand.dto.OrderItemDTO;
import com.campus.secondhand.dto.OrderStatsDTO;
import com.campus.secondhand.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
            return Result.success(orderService.createOrder(dto, currentUserId));
        } catch (RuntimeException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @GetMapping("/my/buy")
    public Result<IPage<OrderItemDTO>> myBuyOrders(@RequestParam(defaultValue = "1") Integer current,
                                                   @RequestParam(defaultValue = "10") Integer size,
                                                   HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        return Result.success(orderService.listMyBuyOrders(currentUserId, current, size));
    }

    @GetMapping("/my/sell")
    public Result<IPage<OrderItemDTO>> mySellOrders(@RequestParam(defaultValue = "1") Integer current,
                                                    @RequestParam(defaultValue = "10") Integer size,
                                                    HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        return Result.success(orderService.listMySellOrders(currentUserId, current, size));
    }

    @PostMapping("/cancel")
    public Result<String> cancelOrder(@RequestParam String orderNo, HttpServletRequest request) {
        try {
            Long currentUserId = (Long) request.getAttribute("currentUserId");
            orderService.cancelOrder(orderNo, currentUserId);
            return Result.success("订单已取消");
        } catch (RuntimeException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PostMapping("/confirm")
    public Result<String> confirmOrder(@RequestParam String orderNo, HttpServletRequest request) {
        try {
            Long currentUserId = (Long) request.getAttribute("currentUserId");
            orderService.confirmOrder(orderNo, currentUserId);
            return Result.success("已确认收货");
        } catch (RuntimeException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @GetMapping("/stats")
    public Result<OrderStatsDTO> getStats(HttpServletRequest request) {
        if (!"admin".equals(request.getAttribute("currentUserRole"))) {
            return Result.error("无权限操作");
        }
        return Result.success(orderService.getStats());
    }
}

package com.campus.secondhand.dto;

import lombok.Data;

@Data
public class OrderStatsDTO {
    private long totalOrders;
    private long pendingOrders;
    private long completedOrders;
    private long cancelledOrders;
}

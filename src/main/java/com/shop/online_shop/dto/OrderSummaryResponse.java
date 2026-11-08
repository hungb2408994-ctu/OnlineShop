package com.shop.online_shop.dto;

import java.math.BigDecimal;

public class OrderSummaryResponse {

    private Long totalOrders;
    private Long pendingOrders;
    private Long confirmedOrders;
    private Long shippingOrders;
    private Long completedOrders;
    private Long cancelledOrders;
    private BigDecimal completedAmount;

    public OrderSummaryResponse(
            Long totalOrders,
            Long pendingOrders,
            Long confirmedOrders,
            Long shippingOrders,
            Long completedOrders,
            Long cancelledOrders,
            BigDecimal completedAmount) {

        this.totalOrders = totalOrders;
        this.pendingOrders = pendingOrders;
        this.confirmedOrders = confirmedOrders;
        this.shippingOrders = shippingOrders;
        this.completedOrders = completedOrders;
        this.cancelledOrders = cancelledOrders;
        this.completedAmount = completedAmount;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public Long getPendingOrders() {
        return pendingOrders;
    }

    public Long getConfirmedOrders() {
        return confirmedOrders;
    }

    public Long getShippingOrders() {
        return shippingOrders;
    }

    public Long getCompletedOrders() {
        return completedOrders;
    }

    public Long getCancelledOrders() {
        return cancelledOrders;
    }

    public BigDecimal getCompletedAmount() {
        return completedAmount;
    }
}
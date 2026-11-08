package com.shop.online_shop.dto;

import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;
    private Long orderId;
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;

    public NotificationResponse(
            Long id,
            Long orderId,
            String title,
            String message,
            boolean read,
            LocalDateTime createdAt) {

        this.id = id;
        this.orderId = orderId;
        this.title = title;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
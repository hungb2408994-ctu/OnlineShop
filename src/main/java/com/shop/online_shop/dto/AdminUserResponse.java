package com.shop.online_shop.dto;

import com.shop.online_shop.entity.User;
import java.time.LocalDateTime;

public class AdminUserResponse {

    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private boolean active;
    private LocalDateTime createdAt;

    public AdminUserResponse(User user) {
        this.userId = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.role = user.getRole();
        this.active = user.isActive();
        this.createdAt = user.getCreatedAt();
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
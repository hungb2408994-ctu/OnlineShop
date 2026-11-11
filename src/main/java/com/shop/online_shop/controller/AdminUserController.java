package com.shop.online_shop.controller;

import com.shop.online_shop.dto.AdminUserResponse;
import com.shop.online_shop.service.AdminUserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    // 1. Danh sách người dùng
    @GetMapping
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    // 2. Tìm kiếm người dùng
    @GetMapping("/search")
    public ResponseEntity<List<AdminUserResponse>> searchUsers(
            @RequestParam String keyword
    ) {
        return ResponseEntity.ok(
                adminUserService.searchUsers(keyword.trim())
        );
    }

    // 3. Chi tiết người dùng
    @GetMapping("/{id}")
    public ResponseEntity<AdminUserResponse> getUserById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                adminUserService.getUserById(id)
        );
    }

    // 4. Khóa / mở khóa tài khoản
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request,
            Authentication authentication
    ) {
        Boolean active = request.get("active");

        if (active == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Thiếu trạng thái active"));
        }

        AdminUserResponse response =
                adminUserService.updateUserStatus(
                        id,
                        active,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }
}
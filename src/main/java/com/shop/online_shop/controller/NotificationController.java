
package com.shop.online_shop.controller;

import com.shop.online_shop.dto.NotificationResponse;
import com.shop.online_shop.entity.User;
import com.shop.online_shop.repository.UserRepository;
import com.shop.online_shop.service.NotificationService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(
            NotificationService notificationService,
            UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    // Lấy thông báo của người dùng hiện tại
    @GetMapping
    public List<NotificationResponse> getNotifications(
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        return notificationService.getNotifications(userId);
    }

    // Đếm số thông báo chưa đọc
    @GetMapping("/unread-count")
    public long getUnreadCount(Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        return notificationService.getUnreadCount(userId);
    }

    // Đánh dấu một thông báo đã đọc
    @PutMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        notificationService.markAsRead(id, userId);
    }

    // Đánh dấu tất cả thông báo đã đọc
    @PutMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllAsRead(Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        notificationService.markAllAsRead(userId);
    }

    // Xóa một thông báo
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotification(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        notificationService.deleteNotification(id, userId);
    }

    // Lấy ID người dùng từ JWT
    private Long getCurrentUserId(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "You must login first!"
            );
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found!"
                ));

        return user.getId();
    }
}

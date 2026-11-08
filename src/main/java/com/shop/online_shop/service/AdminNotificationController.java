package com.shop.online_shop.service;

import com.shop.online_shop.entity.User;
import com.shop.online_shop.repository.UserRepository;
import com.shop.online_shop.service.NotificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
public class AdminNotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public AdminNotificationController(
            NotificationService notificationService,
            UserRepository userRepository) {

        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    private User getCurrentAdmin(Authentication authentication) {

        if (authentication == null) {
            throw new RuntimeException("Unauthorized!");
        }

        User admin = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Admin not found!"));

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            throw new RuntimeException("Access denied!");
        }

        return admin;
    }

    @GetMapping
    public ResponseEntity<?> getNotifications(
            Authentication authentication) {

        User admin = getCurrentAdmin(authentication);

        return ResponseEntity.ok(
                notificationService.getNotifications(admin.getId())
        );
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(
            Authentication authentication) {

        User admin = getCurrentAdmin(authentication);

        return ResponseEntity.ok(Map.of(
                "unreadCount",
                notificationService.getUnreadCount(admin.getId())
        ));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {

        User admin = getCurrentAdmin(authentication);

        notificationService.markAsRead(id, admin.getId());

        return ResponseEntity.ok(Map.of(
                "message", "Notification marked as read!"
        ));
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(
            Authentication authentication) {

        User admin = getCurrentAdmin(authentication);

        notificationService.markAllAsRead(admin.getId());

        return ResponseEntity.ok(Map.of(
                "message", "All notifications marked as read!"
        ));
    }
}
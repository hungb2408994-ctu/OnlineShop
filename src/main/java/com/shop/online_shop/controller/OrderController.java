
package com.shop.online_shop.controller;
import com.shop.online_shop.dto.OrderResponse;
import com.shop.online_shop.dto.CheckoutRequest;
import com.shop.online_shop.entity.User;
import com.shop.online_shop.repository.UserRepository;
import com.shop.online_shop.service.OrderService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

import com.shop.online_shop.dto.OrderSummaryResponse;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(
            OrderService orderService,
            UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    private boolean isOwner(Long userId, Authentication authentication) {

        if (authentication == null) {
            return false;
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found!"));

        return user.getId().equals(userId);
    }

    @PostMapping("/checkout/{userId}")
    public ResponseEntity<?> checkout(
            @PathVariable Long userId,
            @RequestBody CheckoutRequest request,
            Authentication authentication) {

        try {
            if (!isOwner(userId, authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Access denied!"));
            }

            OrderResponse order = orderService.checkout(
                    userId,
                    request.getShippingAddress()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(order);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getOrderHistory(
            @PathVariable Long userId,
            Authentication authentication) {

        try {
            if (!isOwner(userId, authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Access denied!"));
            }

            return ResponseEntity.ok(
                    orderService.getOrderHistory(userId)
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{userId}/summary")
    public ResponseEntity<?> getOrderSummary(
            @PathVariable Long userId,
            Authentication authentication) {

        try {
            if (!isOwner(userId, authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Access denied!"));
            }

            OrderSummaryResponse summary =
                    orderService.getOrderSummary(userId);

            return ResponseEntity.ok(summary);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{orderId}/total")
    public ResponseEntity<?> calculateOrderTotal(
            @PathVariable Long orderId,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please login first!"));
        }

        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found!"));

            BigDecimal total = orderService.calculateOrderTotal(
                    orderId,
                    user.getId()
            );

            return ResponseEntity.ok(total);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
}

}

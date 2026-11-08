
package com.shop.online_shop.controller;

import com.shop.online_shop.dto.OrderResponse;
import com.shop.online_shop.service.OrderService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetails(
            @PathVariable Long orderId,
            Authentication authentication) {

        try {
            if (authentication == null ||
                    authentication.getAuthorities().stream()
                            .noneMatch(authority ->
                                    authority.getAuthority().equals("ROLE_ADMIN"))) {

                return ResponseEntity.status(403)
                        .body(Map.of("message",
                                "Bạn không có quyền truy cập!"));
            }

            OrderResponse order =
                    orderService.getOrderDetails(orderId);

            return ResponseEntity.ok(order);

        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}

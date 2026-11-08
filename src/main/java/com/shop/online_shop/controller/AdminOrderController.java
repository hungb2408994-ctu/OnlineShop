
package com.shop.online_shop.controller;

import com.shop.online_shop.dto.OrderResponse;
import com.shop.online_shop.service.OrderService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // GET: Tất cả đơn hàng
    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // GET: Chi tiết đơn hàng
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(
            @PathVariable Long orderId) {

        try {
            return ResponseEntity.ok(
                    orderService.getOrderById(orderId)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // GET: Tìm kiếm đơn hàng
    @GetMapping("/search")
    public ResponseEntity<List<OrderResponse>> searchOrders(
            @RequestParam String keyword) {

        return ResponseEntity.ok(
                orderService.searchOrders(keyword)
        );
    }

    // GET: Lọc theo trạng thái
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(
            @PathVariable String status) {

        try {
            return ResponseEntity.ok(
                    orderService.getOrdersByStatus(status)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // PUT: Cập nhật trạng thái đơn hàng
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {

        try {
            String status = request.get("status");

            OrderResponse response =
                    orderService.updateOrderStatus(orderId, status);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            HttpStatus httpStatus = e.getMessage() != null
                    && e.getMessage().equals("Order not found!")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(httpStatus)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}

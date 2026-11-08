package com.shop.online_shop.service;

import com.shop.online_shop.dto.OrderItemResponse;
import com.shop.online_shop.dto.OrderResponse;
import com.shop.online_shop.dto.OrderSummaryResponse;
import com.shop.online_shop.entity.*;
import com.shop.online_shop.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

        private final OrderRepository orderRepository;
        private final CartRepository cartRepository;
        private final UserRepository userRepository;
        private final ProductRepository productRepository;
        private final JdbcTemplate jdbcTemplate;
        private final NotificationRepository notificationRepository;

    public OrderService(
        OrderRepository orderRepository,
        CartRepository cartRepository,
        UserRepository userRepository,
        ProductRepository productRepository,
        NotificationRepository notificationRepository,
        JdbcTemplate jdbcTemplate) {

        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public OrderResponse checkout(Long userId, String shippingAddress) {

        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            throw new RuntimeException("Shipping address cannot be empty!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found!"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty!");
        }

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setStatus("PENDING");

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItemResponse> responseItems = new ArrayList<>();

        for (CartItem cartItem : new ArrayList<>(cart.getItems())) {

            Product product = productRepository.findById(
                    cartItem.getProduct().getId()
            ).orElseThrow(() -> new RuntimeException("Product not found!"));

            Integer quantity = cartItem.getQuantity();

            if (quantity == null || quantity <= 0) {
                throw new RuntimeException("Invalid product quantity!");
            }

            if (product.getQuantity() == null ||
                    product.getQuantity() < quantity) {
                throw new RuntimeException(
                        "Insufficient stock for product: " + product.getName()
                );
            }

            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(quantity));

            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(product.getPrice());

            order.getItems().add(orderItem);

            product.setQuantity(product.getQuantity() - quantity);
            productRepository.save(product);

            responseItems.add(new OrderItemResponse(
                    product.getId(),
                    product.getName(),
                    quantity,
                    product.getPrice(),
                    subtotal
            ));
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        // Tạo thông báo cho tất cả Admin
        List<User> admins = userRepository.findAllByRoleIgnoreCase("ADMIN");

        for (User admin : admins) {

        Notification notification = new Notification();

        notification.setUser(admin);
        notification.setOrder(savedOrder);
        notification.setTitle("Đơn hàng mới");

        notification.setMessage(
                "Khách hàng " + user.getFullName()
                + " vừa đặt đơn hàng #" + savedOrder.getId()
        );

        notification.setRead(false);

        notificationRepository.save(notification);
        }

        cart.getItems().clear();
        cartRepository.save(cart);

        return new OrderResponse(
                savedOrder.getId(),
                user.getId(),
                savedOrder.getOrderDate(),
                totalAmount,
                savedOrder.getStatus(),
                savedOrder.getShippingAddress(),
                responseItems
        );
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrderHistory(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        List<Order> orders =
                orderRepository.findByUserIdOrderByOrderDateDesc(userId);

        List<OrderResponse> responses = new ArrayList<>();

        for (Order order : orders) {

            List<OrderItemResponse> items = new ArrayList<>();

            for (OrderItem item : order.getItems()) {

                items.add(new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice().multiply(
                                BigDecimal.valueOf(item.getQuantity())
                        )
                ));
            }

            responses.add(new OrderResponse(
                    order.getId(),
                    userId,
                    order.getOrderDate(),
                    order.getTotalAmount(),
                    order.getStatus(),
                    order.getShippingAddress(),
                    items
            ));
        }

        return responses;
    }

        @Transactional(readOnly = true)
        public BigDecimal calculateOrderTotal(Long orderId, Long userId) {

        boolean isOwner = orderRepository.existsByIdAndUser_Id(orderId, userId);

        if (!isOwner) {
                throw new RuntimeException("Order not found!");
        }

        return orderRepository.calculateOrderTotal(orderId);
        }
        @Transactional(readOnly = true)
        public OrderSummaryResponse getOrderSummary(Long userId) {

        return jdbcTemplate.queryForObject(
                "CALL sp_user_order_summary(?)",
                (rs, rowNum) -> new OrderSummaryResponse(
                        rs.getLong("total_orders"),
                        rs.getLong("pending_orders"),
                        rs.getLong("confirmed_orders"),
                        rs.getLong("shipping_orders"),
                        rs.getLong("completed_orders"),
                        rs.getLong("cancelled_orders"),
                        rs.getBigDecimal("completed_amount")
                ),
                userId
        );
        }

        @Transactional(readOnly = true)
        public OrderResponse getOrderDetails(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found!"));

        List<OrderItemResponse> items = new ArrayList<>();

        for (OrderItem item : order.getItems()) {

                BigDecimal subtotal = item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));

                items.add(new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        subtotal
                ));
        }

        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getShippingAddress(),
                items
        );
        }

}

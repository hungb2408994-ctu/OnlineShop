
package com.shop.online_shop.repository;

import com.shop.online_shop.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Lịch sử đơn hàng của người dùng
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);

    // Tất cả đơn hàng, mới nhất trước
    List<Order> findAllByOrderByOrderDateDesc();

    // Lọc đơn hàng theo trạng thái
    List<Order> findByStatusIgnoreCaseOrderByOrderDateDesc(String status);

    // Tìm kiếm đơn hàng theo mã, tên, email hoặc địa chỉ
    @Query("""
        SELECT o FROM Order o
        JOIN o.user u
        WHERE CAST(o.id AS string) LIKE CONCAT('%', :keyword, '%')
           OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(o.shippingAddress) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY o.orderDate DESC
    """)
    List<Order> searchOrders(@Param("keyword") String keyword);

    // Tính tổng tiền bằng MySQL function
    @Query(value = "SELECT fn_order_total(:orderId)", nativeQuery = true)
    BigDecimal calculateOrderTotal(@Param("orderId") Long orderId);

    // Kiểm tra đơn hàng có thuộc về người dùng không
    boolean existsByIdAndUser_Id(Long orderId, Long userId);
}

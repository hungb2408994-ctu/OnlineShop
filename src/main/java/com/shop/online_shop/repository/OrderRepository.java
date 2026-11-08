package com.shop.online_shop.repository;

import com.shop.online_shop.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;


public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);

    @Query(value = "SELECT fn_order_total(:orderId)", nativeQuery = true)
    BigDecimal calculateOrderTotal(@Param("orderId") Long orderId);

    boolean existsByIdAndUser_Id(Long orderId, Long userId);

}
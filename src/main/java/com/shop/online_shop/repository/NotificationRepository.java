package com.shop.online_shop.repository;

import com.shop.online_shop.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUser_IdOrderByCreatedAtDesc(Long userId);

    long countByUser_IdAndReadFalse(Long userId);

    Optional<Notification> findByIdAndUser_Id(
            Long id,
            Long userId
    );
}
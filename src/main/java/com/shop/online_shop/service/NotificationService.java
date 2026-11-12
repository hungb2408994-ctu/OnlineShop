package com.shop.online_shop.service;

import com.shop.online_shop.dto.NotificationResponse;
import com.shop.online_shop.entity.Notification;
import com.shop.online_shop.repository.NotificationRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(
            NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long userId) {

        return notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(notification -> new NotificationResponse(
                        notification.getId(),
                        notification.getOrder() != null
                                ? notification.getOrder().getId()
                                : null,
                        notification.getTitle(),
                        notification.getMessage(),
                        notification.isRead(),
                        notification.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUser_IdAndReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {

        Notification notification = notificationRepository
                .findByIdAndUser_Id(notificationId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Notification not found!"));

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {

        List<Notification> notifications = notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId);

        for (Notification notification : notifications) {
            notification.setRead(true);
        }

        notificationRepository.saveAll(notifications);
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {

        Notification notification = notificationRepository
                .findByIdAndUser_Id(notificationId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Notification not found!"));

        notificationRepository.delete(notification);
    }
}
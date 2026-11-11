package com.shop.online_shop.service;

import com.shop.online_shop.dto.AdminUserResponse;
import com.shop.online_shop.entity.User;
import com.shop.online_shop.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminUserService {

    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(AdminUserResponse::new)
                .toList();
    }

    public List<AdminUserResponse> searchUsers(String keyword) {
        return userRepository
                .findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        keyword, keyword
                )
                .stream()
                .map(AdminUserResponse::new)
                .toList();
    }

    public AdminUserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy người dùng"));

        return new AdminUserResponse(user);
    }

    @Transactional
    public AdminUserResponse updateUserStatus(
            Long id,
            boolean active,
            String currentAdminEmail
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy người dùng"));

        if (user.getEmail().equalsIgnoreCase(currentAdminEmail)) {
            throw new RuntimeException(
                    "Không thể khóa tài khoản Admin đang đăng nhập"
            );
        }

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException(
                    "Không thể khóa tài khoản Admin khác"
            );
        }

        user.setActive(active);

        return new AdminUserResponse(userRepository.save(user));
    }
}
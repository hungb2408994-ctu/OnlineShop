
package com.shop.online_shop.controller;

import com.shop.online_shop.dto.LoginRequest;
import com.shop.online_shop.dto.RegisterRequest;
import com.shop.online_shop.entity.User;
import com.shop.online_shop.service.AuthService;
import com.shop.online_shop.service.JwtService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(
            AuthService authService,
            JwtService jwtService) {

        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request) {

        try {
            User user = authService.register(request);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                    "message", "Đăng ký tài khoản thành công!",
                    "userId", user.getId(),
                    "fullName", user.getFullName(),
                    "email", user.getEmail(),
                    "role", user.getRole()
                )
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request) {

        try {
            User user = authService.login(
                request.getEmail(),
                request.getPassword()
            );

            // Tạo JWT sau khi đăng nhập thành công
            String accessToken = jwtService.generateToken(
                user.getEmail()
            );

            ResponseCookie jwtCookie = ResponseCookie.from("accessToken", accessToken)
                    .httpOnly(true)
                    .secure(false) // localhost dùng HTTP; production HTTPS phải đổi thành true
                    .path("/")
                    .maxAge(Duration.ofHours(24))
                    .sameSite("Lax")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(
                        Map.of(
                            "message", "Đăng nhập thành công!",
                            "userId", user.getId(),
                            "fullName", user.getFullName(),
                            "email", user.getEmail(),
                            "role", user.getRole(),
                            "accessToken", accessToken,
                            "tokenType", "Bearer"
                        )
                    );

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of("message", e.getMessage())
            );
        }
    }
}

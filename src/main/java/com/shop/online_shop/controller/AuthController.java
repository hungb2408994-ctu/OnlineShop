package com.shop.online_shop.controller;

import com.shop.online_shop.dto.LoginRequest;
import com.shop.online_shop.dto.RegisterRequest;
import com.shop.online_shop.entity.User;
import com.shop.online_shop.security.AuthCookie;
import com.shop.online_shop.service.AuthService;
import com.shop.online_shop.service.JwtService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        try {
            User user = authService.login(
                request.getEmail(),
                request.getPassword()
            );

            // Tạo JWT sau khi đăng nhập thành công
            String accessToken = jwtService.generateToken(
                user.getEmail()
            );

            // Lưu JWT vào cookie để các trang server-side (/admin/**) xác thực được.
            // Giao diện vẫn dùng Authorization: Bearer cho các lời gọi API.
            response.addHeader(
                HttpHeaders.SET_COOKIE,
                buildAccessTokenCookie(accessToken, jwtService.getExpirationMillis())
                    .toString()
            );

            return ResponseEntity.ok(
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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {

        // Xóa cookie phiên đăng nhập
        response.addHeader(
            HttpHeaders.SET_COOKIE,
            buildAccessTokenCookie("", 0).toString()
        );

        return ResponseEntity.ok(
            Map.of("message", "Đăng xuất thành công!")
        );
    }

    private ResponseCookie buildAccessTokenCookie(String value, long maxAgeMillis) {

        return ResponseCookie.from(AuthCookie.ACCESS_TOKEN, value)
                .httpOnly(true)
                // Chỉ bật secure(true) khi chạy HTTPS
                .secure(false)
                .path("/")
                .maxAge(Duration.ofMillis(maxAgeMillis))
                .sameSite("Lax")
                .build();
    }
}

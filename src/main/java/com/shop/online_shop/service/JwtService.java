
package com.shop.online_shop.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration}")
    private long expiration;

    // Tạo khóa bí mật để ký JWT
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    // Tạo JWT Token từ email người dùng
    public String generateToken(String email) {

        Date now = new Date();

        Date expiryDate = new Date(
                now.getTime() + expiration
        );

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // Lấy email từ JWT Token
    public String extractUsername(String token) {

        return extractAllClaims(token).getSubject();
    }

    // Thời gian sống của token (mili giây), dùng để đặt Max-Age cho cookie
    public long getExpirationMillis() {

        return expiration;
    }

    // Kiểm tra Token có hợp lệ không
    public boolean isTokenValid(String token) {

        try {
            extractAllClaims(token);
            return true;

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Đọc toàn bộ thông tin trong Token
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

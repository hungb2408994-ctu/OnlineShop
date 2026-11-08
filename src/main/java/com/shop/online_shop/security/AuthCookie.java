package com.shop.online_shop.security;

/**
 * Tên cookie lưu JWT. Cookie được gửi tự động khi trình duyệt mở trang
 * (ví dụ /admin/**) nên các trang server-side mới xác thực được người dùng.
 */
public final class AuthCookie {

    public static final String ACCESS_TOKEN = "accessToken";

    private AuthCookie() {
    }
}

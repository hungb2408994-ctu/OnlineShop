package com.shop.online_shop.security;

import com.shop.online_shop.repository.UserRepository;
import com.shop.online_shop.service.JwtService;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private static final String FORBIDDEN_PAGE = """
                        <!DOCTYPE html>
                        <html lang="vi">
                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <title>Không có quyền truy cập</title>
                            <style>
                                body { font-family: Arial, sans-serif; background: #f3f6fb; margin: 0; }
                                .box { max-width: 520px; margin: 80px auto; background: #fff; padding: 32px;
                                       border-radius: 12px; box-shadow: 0 8px 30px rgba(0, 0, 0, .08); }
                                h1 { color: #172033; font-size: 22px; margin: 0 0 12px; }
                                p { color: #475569; line-height: 1.6; }
                                a { color: #2563eb; font-weight: 600; text-decoration: none; }
                            </style>
                        </head>
                        <body>
                            <div class="box">
                                <h1>Không có quyền truy cập</h1>
                                <p>
                                    Tài khoản bạn đang đăng nhập không có quyền <strong>ADMIN</strong>,
                                    nên không thể mở trang quản trị.
                                </p>
                                <p>
                                    Hãy đăng nhập bằng tài khoản quản trị viên hoặc
                                    <a href="/">quay lại trang chủ</a>.
                                </p>
                            </div>
                        </body>
                        </html>
                        """;

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        JwtService jwtService,
                        UserRepository userRepository) throws Exception {

                // Tạo filter ngay trong security chain (không khai báo @Bean) để Spring Boot
                // không tự đăng ký nó thêm một lần nữa ở cấp servlet, tránh việc
                // SecurityContext bị xoá và request có JWT bị coi là chưa đăng nhập.
                JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService,
                                userRepository);

                http
                                .csrf(csrf -> csrf.disable())

                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint((request, response,
                                                                ex) -> handleUnauthenticated(request, response))
                                                .accessDeniedHandler((request, response,
                                                                ex) -> handleAccessDenied(request, response)))

                                .authorizeHttpRequests(auth -> auth

                                                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                                                // Trang chủ, trang sản phẩm và tài nguyên giao diện
                                                .requestMatchers(
                                                                "/",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/images/**",
                                                                "/favicon.ico",
                                                                "/cart",
                                                                "/products",
                                                                "/products/**",
                                                                "/login",
                                                                "/register",
                                                                "/checkout",
                                                                "/orders",
                                                                "/my-orders",
                                                                "/notifications")
                                                .permitAll()

                                                // Chỉ ADMIN được truy cập trang quản trị
                                                .requestMatchers("/admin/**").hasRole("ADMIN")

                                                // API đăng ký, đăng nhập và đăng xuất
                                                .requestMatchers(
                                                                "/api/auth/register",
                                                                "/api/auth/login",
                                                                "/api/auth/logout")
                                                .permitAll()

                                                // Chỉ ADMIN được thêm sản phẩm và danh mục
                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/products/**",
                                                                "/api/categories/**")
                                                .hasRole("ADMIN")

                                                // Chỉ ADMIN được cập nhật
                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/api/products/**",
                                                                "/api/categories/**")
                                                .hasRole("ADMIN")

                                                // Chỉ ADMIN được xóa
                                                .requestMatchers(
                                                                HttpMethod.DELETE,
                                                                "/api/products/**",
                                                                "/api/categories/**")
                                                .hasRole("ADMIN")

                                                // Mọi người đều được xem sản phẩm và danh mục
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/products/**",
                                                                "/api/categories/**")
                                                .permitAll()

                                                // API quản trị
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                                                .requestMatchers("/my-orders").permitAll()
                                                .requestMatchers("/notifications").permitAll()
                                                // Những API còn lại yêu cầu đăng nhập
                                                .anyRequest().authenticated())

                                .addFilterBefore(
                                                jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        /**
         * Chưa đăng nhập: trang HTML thì chuyển sang /login (kèm trang muốn mở),
         * API thì trả về JSON 401.
         */
        private void handleUnauthenticated(
                        HttpServletRequest request,
                        HttpServletResponse response) throws IOException {

                if (isHtmlRequest(request)) {
                        String next = URLEncoder.encode(
                                        buildRequestPath(request),
                                        StandardCharsets.UTF_8);

                        response.sendRedirect("/login?next=" + next);
                        return;
                }

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                                "{\"status\":401,\"error\":\"Unauthorized\","
                                                + "\"message\":\"Bạn cần đăng nhập để thực hiện chức năng này!\"}");
        }

        /**
         * Đã đăng nhập nhưng không đủ quyền: trang HTML trả về trang 403 dễ hiểu,
         * API trả về JSON 403.
         */
        private void handleAccessDenied(
                        HttpServletRequest request,
                        HttpServletResponse response) throws IOException {

                if (isHtmlRequest(request)) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("text/html;charset=UTF-8");
                        response.getWriter().write(FORBIDDEN_PAGE);
                        return;
                }

                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                                "{\"status\":403,\"error\":\"Forbidden\","
                                                + "\"message\":\"Bạn không có quyền truy cập chức năng này!\"}");
        }

        private boolean isHtmlRequest(HttpServletRequest request) {

                String accept = request.getHeader("Accept");

                return accept != null && accept.contains("text/html");
        }

        private String buildRequestPath(HttpServletRequest request) {

                String path = request.getRequestURI();
                String query = request.getQueryString();

                return query == null ? path : path + "?" + query;
        }
}

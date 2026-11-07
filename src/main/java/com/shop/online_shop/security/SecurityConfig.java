
package com.shop.online_shop.security;

import com.shop.online_shop.repository.UserRepository;
import com.shop.online_shop.service.JwtService;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository) {

        return new JwtAuthenticationFilter(jwtService, userRepository);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, ex) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Bạn không có quyền truy cập chức năng này!\"}");
                        }))

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
                                "/orders"
                        ).permitAll()

                        // Chỉ ADMIN được truy cập trang quản trị
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // API đăng ký và đăng nhập
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        // Chỉ ADMIN được thêm sản phẩm và danh mục
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/products/**",
                                "/api/categories/**"
                        ).hasRole("ADMIN")

                        // Chỉ ADMIN được cập nhật
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/products/**",
                                "/api/categories/**"
                        ).hasRole("ADMIN")

                        // Chỉ ADMIN được xóa
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/products/**",
                                "/api/categories/**"
                        ).hasRole("ADMIN")

                        // Mọi người đều được xem sản phẩm và danh mục
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/products/**",
                                "/api/categories/**"
                        ).permitAll()

                        // API quản trị
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Những API còn lại yêu cầu đăng nhập
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

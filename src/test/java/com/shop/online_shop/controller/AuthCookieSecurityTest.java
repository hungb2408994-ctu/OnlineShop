package com.shop.online_shop.controller;

import com.shop.online_shop.entity.User;
import com.shop.online_shop.repository.UserRepository;
import com.shop.online_shop.security.SecurityConfig;
import com.shop.online_shop.service.AuthService;
import com.shop.online_shop.service.JwtService;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Kiểm thử việc cấp JWT bằng cookie để trình duyệt mở được các trang /admin/**.
 */
@WebMvcTest(controllers = { AuthController.class, AdminController.class })
@Import(SecurityConfig.class)
class AuthCookieSecurityTest {

    private static final String TOKEN = "jwt-token";
    private static final String ADMIN_EMAIL = "admin@test.local";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void loginSetsAccessTokenCookie() throws Exception {

        given(authService.login(ADMIN_EMAIL, "123456")).willReturn(user("ADMIN"));
        given(jwtService.generateToken(ADMIN_EMAIL)).willReturn(TOKEN);
        given(jwtService.getExpirationMillis()).willReturn(86_400_000L);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + ADMIN_EMAIL + "\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.accessToken").value(TOKEN))
                .andExpect(header().string("Set-Cookie", allOf(
                        containsString("accessToken=" + TOKEN),
                        containsString("HttpOnly"),
                        containsString("Path=/"),
                        containsString("Max-Age=86400")
                )));
    }

    @Test
    void logoutClearsAccessTokenCookie() throws Exception {

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", allOf(
                        containsString("accessToken="),
                        containsString("Max-Age=0")
                )));
    }

    @Test
    void unauthenticatedAdminPageRedirectsToLogin() throws Exception {

        mockMvc.perform(get("/admin/products").header("Accept", "text/html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location",
                        containsString("/login?next=")));
    }

    @Test
    void adminCookieGrantsAccessToAdminApi() throws Exception {

        given(jwtService.isTokenValid(TOKEN)).willReturn(true);
        given(jwtService.extractUsername(TOKEN)).willReturn(ADMIN_EMAIL);
        given(userRepository.findByEmail(ADMIN_EMAIL))
                .willReturn(Optional.of(user("ADMIN")));

        mockMvc.perform(get("/api/admin/test")
                        .cookie(new Cookie("accessToken", TOKEN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void bearerTokenStillGrantsAccessToAdminApi() throws Exception {

        given(jwtService.isTokenValid(TOKEN)).willReturn(true);
        given(jwtService.extractUsername(TOKEN)).willReturn(ADMIN_EMAIL);
        given(userRepository.findByEmail(ADMIN_EMAIL))
                .willReturn(Optional.of(user("ADMIN")));

        mockMvc.perform(get("/api/admin/test")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void apiWithoutCookieReturnsUnauthorized() throws Exception {

        mockMvc.perform(get("/api/admin/test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userRoleCookieCanNotOpenAdminPage() throws Exception {

        given(jwtService.isTokenValid(TOKEN)).willReturn(true);
        given(jwtService.extractUsername(TOKEN)).willReturn(ADMIN_EMAIL);
        given(userRepository.findByEmail(ADMIN_EMAIL))
                .willReturn(Optional.of(user("USER")));

        mockMvc.perform(get("/admin/products")
                        .header("Accept", "text/html")
                        .cookie(new Cookie("accessToken", TOKEN)))
                .andExpect(status().isForbidden());
    }

    private User user(String role) {

        User user = new User();
        user.setId(1L);
        user.setFullName("Nguyen Van A");
        user.setEmail(ADMIN_EMAIL);
        user.setRole(role);

        return user;
    }
}

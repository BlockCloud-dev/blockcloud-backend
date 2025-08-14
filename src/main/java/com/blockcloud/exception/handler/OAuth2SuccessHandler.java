package com.blockcloud.exception.handler;

import com.blockcloud.domain.user.User;
import com.blockcloud.dto.oauth.CustomOAuth2User;
import com.blockcloud.jwt.JWTUtil;
import com.blockcloud.service.CookieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;


@AllArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final CookieService cookieService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = customOAuth2User.getUser();

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String accessToken = jwtUtil.createJwt("access", user.getEmail(), String.valueOf(user.getRole()), 60 * 1000L);
            String refreshToken = jwtUtil.createJwt("refresh", user.getEmail(), String.valueOf(user.getRole()), 24 * 60 * 60 * 1000L);

            Cookie refreshCookie = cookieService.createCookie("refresh", refreshToken, 24 * 60 * 60 * 1000L);
            response.addCookie(refreshCookie);

            String userJson = objectMapper.writeValueAsString(
                Map.of(
                    "message", "Login successful",
                    "email", user.getEmail(),
                    "imgUrl", user.getImgUrl(),
                    "userName", user.getUsername(),
                    "role", user.getRole()
                )
            );

            String encodedJson = URLEncoder.encode(userJson, StandardCharsets.UTF_8);

            String uri = UriComponentsBuilder
                .newInstance()
                .scheme("https")
                .host("blockcloud.dev")
                .path("/login/success")
                .queryParam("user", encodedJson)
                .queryParam("access", accessToken)
                .build()
                .toString();

            response.sendRedirect(uri);

        } catch (IOException e) {
            e.printStackTrace(); // 로깅
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType("application/json;charset=UTF-8");
            
            // finally 블록 대신 try-with-resources 사용
            try (PrintWriter writer = response.getWriter()) {
                writer.write("{\"message\": \"An error occurred during authentication\"}");
                writer.flush();
            }
        }
    }
}

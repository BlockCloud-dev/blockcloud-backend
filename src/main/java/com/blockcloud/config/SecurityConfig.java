package com.blockcloud.config;

import com.blockcloud.dto.common.ExceptionDto;
import com.blockcloud.dto.common.ResponseDto;
import com.blockcloud.exception.error.ErrorCode;
import com.blockcloud.exception.handler.CustomLogoutSuccessHandler;
import com.blockcloud.exception.handler.OAuth2SuccessHandler;
import com.blockcloud.jwt.JWTFilter;
import com.blockcloud.jwt.JWTUtil;
import com.blockcloud.service.CookieService;
import com.blockcloud.service.CustomOAuth2UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor

public class SecurityConfig {

    private final JWTUtil jwtUtil;
    private final CookieService cookieService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/login/success",
                    "/signup/success",
                    "/error",
                    "/login/oauth2/code/google",
                    "/swagger-ui",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/actuator/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(new OAuth2SuccessHandler(jwtUtil, cookieService))
            )
            // 401 Unauthorized 에러를 공통 응답 포맷으로 변경
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");

                    ExceptionDto errorDto = ExceptionDto.of(ErrorCode.AUTHENTICATION_REQUIRED);

                    // 공통 응답 DTO로 감싸기
                    ResponseDto<Object> errorResponse = ResponseDto.builder()
                        .httpStatus(HttpStatus.UNAUTHORIZED)
                        .success(false)
                        .data(null)
                        .error(errorDto)
                        .build();

                    // ObjectMapper를 사용하여 JSON으로 변환 후 응답
                    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                })
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .deleteCookies("refresh")
                .logoutSuccessHandler(new CustomLogoutSuccessHandler())
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "cookie"));
        configuration.setExposedHeaders(List.of("Authorization", "verify"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

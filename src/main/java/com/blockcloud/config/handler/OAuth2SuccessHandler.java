package com.blockcloud.config.handler;

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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;


@AllArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

	private final JWTUtil jwtUtil;
	private final CookieService cookieService;


	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
		// Authentication 객체에서 CustomOAuth2User 정보 가져오기
		CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
		User user = customOAuth2User.getUser();  // User 정보 가져오기

		// ObjectMapper와 PrintWriter를 한번만 생성
		ObjectMapper objectMapper = new ObjectMapper();
		PrintWriter writer = response.getWriter();

		try {
				String accessToken = jwtUtil.createJwt("access", user.getEmail(), String.valueOf(user.getRole()), 60 * 1000L);
				String refreshToken = jwtUtil.createJwt("refresh", user.getEmail(), String.valueOf(user.getRole()), 24 * 60 * 60 * 1000L);

				Cookie refreshCookie = cookieService.createCookie( "refresh", refreshToken, 24 * 60 * 60 * 1000L);
				response.addCookie(refreshCookie);
				response.setStatus(HttpStatus.OK.value());
				String userJson = objectMapper.writeValueAsString(user);

				// JSON 직렬화
				String userjson = objectMapper.writeValueAsString(
					Map.of(
						"message", "Login successful",
						"email", user.getEmail(),
						"imgUrl",user.getImgUrl(),
						"userName",user.getUsername(),
						"role",user.getRole()
					)
				);

				// URL에 JSON을 쿼리 파라미터로 추가
				String encodedJson = URLEncoder.encode(userjson, StandardCharsets.UTF_8);

				String uri = UriComponentsBuilder
					.newInstance()
					.scheme("http")
					.host("localhost:8080")
					.path("/login/success")
					.queryParam("user", encodedJson)
					.queryParam("access", accessToken)
					.build()
					.toString();
				response.sendRedirect(uri);
		} catch (IOException e) {
			// 에러 처리: 500 응답 전송
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			writer.write("{\"message\": \"An error occurred during authentication\"}");
			writer.flush();
		}

	}
}
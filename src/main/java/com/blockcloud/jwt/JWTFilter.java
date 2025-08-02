package com.blockcloud.jwt;

import com.blockcloud.domain.user.User;
import com.blockcloud.dto.oauth.CustomUserDetails;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.thymeleaf.util.StringUtils;

@Component
@AllArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
	private final JWTUtil jwtUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		// 헤더에서 Authorization 키에 담긴 토큰을 꺼냄
		String authorizationHeader = request.getHeader("Authorization");

		// 프리플라이트 요청에 대해 모든 요청을 허용
		if (StringUtils.equals(request.getMethod(), "OPTIONS")) {
			logger.debug("if request method is OPTIONS, allowing request");
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		}
		// Authorization 헤더가 없거나 값이 비어있다면 다음 필터로 넘김
		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		// "Bearer " 이후의 실제 토큰 값을 추출
		String accessToken = authorizationHeader.substring(7);

		// 토큰 만료 여부 확인, 만료시 다음 필터로 넘기지 않음
		try {
			jwtUtil.isExpired(accessToken);
		} catch (ExpiredJwtException e) {

			//response body
			PrintWriter writer = response.getWriter();
			writer.print("access token expired");

			//response status code
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		// 토큰이 access인지 확인 (발급시 페이로드에 명시)
		String category = jwtUtil.getCategory(accessToken);

		if (!category.equals("access")) {

			//response body
			PrintWriter writer = response.getWriter();
			writer.print("invalid access token");

			//response status code
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		// username, role 값을 획득
		String nickname= jwtUtil.getEmail(accessToken);
		String role = jwtUtil.getRole(accessToken);

		User user = User.builder()
			.username(nickname)  // User 클래스의 필드명에 맞게 수정
			.role(role)  // Role.valueOf()는 필요에 따라 유지
			.build();
		CustomUserDetails customUserDetails = new CustomUserDetails(user);

		Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authToken);

		filterChain.doFilter(request, response);
	}
}

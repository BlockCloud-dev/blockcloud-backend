package com.blockcloud.controller;


import com.blockcloud.jwt.JWTUtil;
import com.blockcloud.service.CookieService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@AllArgsConstructor
public class TokenRestController {

	private final JWTUtil jwtUtil;
	private final CookieService cookieService;

	@PostMapping("/token/refresh")
	public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
		//get refresh token
		String refresh = null;
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return new ResponseEntity<>("cookies null", HttpStatus.BAD_REQUEST);
		}
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals("refresh")) {
				refresh = cookie.getValue();
			}
		}

		if (refresh == null) {
			//response status code
			return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
		}

		//expired check
		try {
			jwtUtil.isExpired(refresh);
		} catch (ExpiredJwtException e) {
			//response status code
			return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
		}

		// 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
		String category = jwtUtil.getCategory(refresh);

		if (!category.equals("refresh")) {
			//response status code
			return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
		}

		String email = jwtUtil.getEmail(refresh);
		String role = jwtUtil.getRole(refresh);

        // Create new tokens
        String newAccess = jwtUtil.createJwt("access", email, role, 30 * 60 * 1000L); // 30분
        String refreshToken = jwtUtil.createJwt("refresh", email, role, 24 * 60 * 60 * 1000L);

        // Set refresh token in cookie
        Cookie refreshCookie = cookieService.createCookie("refresh", refreshToken, 24 * 60 * 60 * 1000L);
        response.addCookie(refreshCookie);

        // Return access token in response body as JSON
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("accessToken", newAccess);

        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }
}
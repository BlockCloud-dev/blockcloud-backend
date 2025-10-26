package com.blockcloud.service;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

	public Cookie createCookie(String key, String value, long maxAge) {
		Cookie cookie = new Cookie(key, value);
		cookie.setMaxAge((int) (maxAge / 1000)); // 밀리초를 초 단위로 변환
		cookie.setHttpOnly(true);
		cookie.setSecure(true); // HTTPS 사용 시
		cookie.setDomain("blockcloud.dev");
		cookie.setPath("/");
		return cookie;
	}

}

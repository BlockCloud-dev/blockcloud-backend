package com.blockcloud.dto.oauth;

import com.blockcloud.domain.user.User;
import java.util.Collection;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@AllArgsConstructor
public class CustomOAuth2User implements OAuth2User {
	private final User user;


	@Override
	public Map<String, Object> getAttributes() {
		return null;
	}

	@Override
	public String getName() {
		return user.getUsername();  // 예: principal로 사용할 이메일 반환
	}

	public String getEmail() {
		return user.getEmail();
	}

	public String getRole() {
		return user.getRole();
	}

	public String getImageUrl() {
		return user.getImgUrl();
	}


	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return null;
	}

	public User getUser() {
		return user;
	}
}
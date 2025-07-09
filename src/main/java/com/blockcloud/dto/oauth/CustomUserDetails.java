package com.blockcloud.dto.oauth;

import com.blockcloud.domain.user.User;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
	private final User user;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(user.getRole()));
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;  // 계정이 만료되지 않음
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;  // 계정이 잠기지 않음
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;  // 자격 증명이 만료되지 않음
	}

	@Override
	public boolean isEnabled() {
		return true;  // 계정이 활성화됨
	}



	public String getemail(){
		return user.getEmail();
	}

}

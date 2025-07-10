package com.blockcloud.service;

import com.blockcloud.domain.user.User;
import com.blockcloud.domain.user.UserRepository;
import com.blockcloud.dto.oauth.CustomOAuth2User;
import com.blockcloud.dto.oauth.GoogleResponse;
import com.blockcloud.dto.oauth.OAuth2ResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
	private final UserRepository userRepository;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);

		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		OAuth2ResponseDto oAuth2Response = null;

		if (registrationId.equals("google")) {
			oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
		} else {
			return null;
		}

		String email = oAuth2Response.getEmail();
		User existData = userRepository.findByEmail(email).orElse(null);

		if (existData == null) {
			User user = User.builder()
				.email(oAuth2Response.getEmail())
				.username(oAuth2Response.getName())
				.imgUrl(oAuth2Response.getPicture()) //
				.role("ROLE_USER")  // 기본 역할 설정
				.build();

			userRepository.save(user);

			return new CustomOAuth2User(user);
		} else {
			return new CustomOAuth2User(existData);
		}
	}
}
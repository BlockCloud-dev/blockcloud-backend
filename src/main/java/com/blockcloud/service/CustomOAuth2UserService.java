package com.blockcloud.service;

import com.blockcloud.domain.user.User;
import com.blockcloud.domain.user.UserRepository;
import com.blockcloud.dto.oauth.CustomOAuth2User;
import com.blockcloud.dto.oauth.GoogleResponse;
import com.blockcloud.dto.oauth.OAuth2ResponseDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        try {
            OAuth2User oAuth2User = super.loadUser(userRequest);
            log.info("Google OAuth2 Attributes: {}", oAuth2User.getAttributes());

            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            OAuth2ResponseDto oAuth2Response = null;

            if (registrationId.equals("google")) {
                oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
                log.info("Mapped GoogleResponse Email: {}", oAuth2Response.getEmail());
            } else {
                log.warn("Unsupported registrationId: {}", registrationId);
                throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
            }

            String email = oAuth2Response.getEmail();
            User existData = userRepository.findByEmail(email).orElse(null);

            if (existData == null) {
                log.info("New user found. Email: {}", email);
                User user = User.builder()
                        .email(oAuth2Response.getEmail())
                        .username(oAuth2Response.getName())
                        .imgUrl(oAuth2Response.getPicture())
                        .role("ROLE_USER")
                        .build();

                userRepository.save(user);
                log.info("New user saved successfully. ID: {}", user.getId());

                return new CustomOAuth2User(user);
            } else {
                log.info("Existing user found. Email: {}", email);
                return new CustomOAuth2User(existData);
            }
        } catch (OAuth2AuthenticationException e) {
            log.error("OAuth2AuthenticationException occurred during user loading: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Critical error in CustomOAuth2UserService: Failed to process user data.", e);
            OAuth2Error oauth2Error = new OAuth2Error(
                    "internal_server_error", 
                    "Internal server error during OAuth2 login process: " + e.getMessage(),
                    null
            );
            throw new OAuth2AuthenticationException(oauth2Error, e);
        }
    }
}

package com.blockcloud.dto.oauth;

import com.blockcloud.domain.user.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // import 추가
import org.springframework.security.oauth2.core.user.OAuth2User;

@AllArgsConstructor
public class CustomOAuth2User implements OAuth2User {
    private final User user;

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public String getName() {
       return user.getEmail();
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
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
        return authorities;
    }

    public User getUser() {
       return user;
    }
}

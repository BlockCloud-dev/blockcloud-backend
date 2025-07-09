package com.blockcloud.service;

import com.blockcloud.domain.user.User;
import com.blockcloud.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getUserInfo(String email) {
        // 이메일로 사용자 정보 조회
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    @Transactional
    public String deleteByEmail(String email) {
        try {
            int result = userRepository.deleteByEmail(email);
            if(result == 0) {
                throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
            } else{
                return "회원 탈퇴가 완료되었습니다.";
            }
        } catch (Exception e) {
            throw new RuntimeException("회원 탈퇴 처리 중 오류가 발생했습니다.", e);
        }
    }
}
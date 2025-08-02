package com.blockcloud.controller;

import com.blockcloud.domain.user.User;
import com.blockcloud.jwt.JWTUtil;
import com.blockcloud.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JWTUtil jwtUtil;

    @GetMapping("/me")
    public ResponseEntity<User> getMyInfo(@RequestHeader("Authorization") String authHeader) {
        // "Bearer " 접두사 제거
        String token = authHeader.substring(7);
        
        // 토큰에서 이메일 추출
        String email = jwtUtil.getEmail(token);
        
        // 이메일로 사용자 정보 조회
        User user = userService.getUserInfo(email);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/api/auth/sign-out")
    public ResponseEntity<?> withdraw(@RequestHeader("Authorization") String authHeader,HttpServletResponse response) {
        try {
            // 1. 토큰 검증
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("유효하지 않은 인증 토큰입니다.");
            }

            String token = authHeader.substring(7);
            String email = jwtUtil.getEmail(token); // email을 사용하는 것이 더 안전

            // 2. 회원 탈퇴 처리
            String result = userService.deleteByEmail(email); // email 기반으로 삭제

            // 3. 모든 인증 정보 무효화
            Cookie refreshTokenCookie = new Cookie("refresh", null);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(0);
            response.addCookie(refreshTokenCookie);


            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("회원 탈퇴 처리 중 오류가 발생했습니다.");
        }
    }

}

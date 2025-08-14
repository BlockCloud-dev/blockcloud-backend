@AllArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final CookieService cookieService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = customOAuth2User.getUser();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String accessToken = jwtUtil.createJwt("access", user.getEmail(), String.valueOf(user.getRole()), 60 * 1000L);
            String refreshToken = jwtUtil.createJwt("refresh", user.getEmail(), String.valueOf(user.getRole()), 24 * 60 * 60 * 1000L);

            Cookie refreshCookie = cookieService.createCookie("refresh", refreshToken, 24 * 60 * 60 * 1000L);
            response.addCookie(refreshCookie);

            // 리다이렉트 대신 JSON 응답을 직접 보냄
            response.setStatus(HttpStatus.OK.value());
            response.setContentType("application/json;charset=UTF-8");

            String userJson = objectMapper.writeValueAsString(
                Map.of(
                    "message", "Login successful",
                    "email", user.getEmail(),
                    "imgUrl", user.getImgUrl(),
                    "userName", user.getUsername(),
                    "role", user.getRole(),
                    "access", accessToken // 액세스 토큰도 응답 본문에 포함
                )
            );
            
            try (PrintWriter writer = response.getWriter()) {
                writer.write(userJson);
                writer.flush();
            }

        } catch (IOException e) {
            e.printStackTrace(); // 로깅
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType("application/json;charset=UTF-8");

            try (PrintWriter writer = response.getWriter()) {
                writer.write("{\"message\": \"An error occurred during authentication\"}");
                writer.flush();
            }
        }
    }
}

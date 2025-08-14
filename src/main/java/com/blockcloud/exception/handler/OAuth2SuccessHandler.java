@Override
public void onAuthenticationSuccess(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Authentication authentication) throws IOException {
    CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
    User user = customOAuth2User.getUser();
    ObjectMapper objectMapper = new ObjectMapper();

    try {
        String accessToken = jwtUtil.createJwt("access", user.getEmail(), String.valueOf(user.getRole()), 60 * 1000L);
        String refreshToken = jwtUtil.createJwt("refresh", user.getEmail(), String.valueOf(user.getRole()), 24 * 60 * 60 * 1000L);

        Cookie refreshCookie = cookieService.createCookie("refresh", refreshToken, 24 * 60 * 60 * 1000L);
        response.addCookie(refreshCookie);

        String userjson = objectMapper.writeValueAsString(
            Map.of(
                "message", "Login successful",
                "email", user.getEmail(),
                "imgUrl", user.getImgUrl(),
                "userName", user.getUsername(),
                "role", user.getRole()
            )
        );

        String encodedJson = URLEncoder.encode(userjson, StandardCharsets.UTF_8);

        String uri = UriComponentsBuilder
            .newInstance()
            .scheme("https")
            .host("blockcloud.dev")
            .path("/login/success")
            .queryParam("user", encodedJson)
            .queryParam("access", accessToken)
            .build()
            .toString();

        response.sendRedirect(uri);

    } catch (IOException e) {
        e.printStackTrace();
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setContentType("application/json;charset=UTF-8");

        try (PrintWriter writer = response.getWriter()) {
            writer.write("{\"message\": \"An error occurred during authentication\"}");
            writer.flush();
        }
    }
}

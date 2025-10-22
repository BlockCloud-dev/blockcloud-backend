package com.blockcloud.controller;

import com.blockcloud.dto.ResponseDto.ProjectShareTokenResponseDto;
import com.blockcloud.dto.common.ResponseDto;
import com.blockcloud.dto.oauth.CustomUserDetails;
import com.blockcloud.service.ProjectShareTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 프로젝트 공유 토큰 관리 API 컨트롤러
 */
@Tag(name = "Project Share Token API", description = "프로젝트 공유 토큰 생성, 조회, 재생성, 비활성화 관련 API")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectShareTokenController {

	private final ProjectShareTokenService projectShareTokenService;

	/**
	 * 프로젝트의 공유 토큰을 생성하거나 조회합니다.
	 *
	 * @param projectId      프로젝트 ID
	 * @param authentication 인증 정보
	 * @return 공유 토큰 정보
	 */
	@Operation(
		summary = "공유 토큰 생성/조회",
		description = "프로젝트의 공유 토큰을 생성하거나 기존 토큰을 조회합니다. JWT 인증 필요."
	)
	@GetMapping("/{projectId}/share-token")
	public ResponseDto<ProjectShareTokenResponseDto> getShareToken(
		@Parameter(description = "프로젝트 ID", required = true)
		@PathVariable Long projectId,
		Authentication authentication) {
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		return ResponseDto.ok(
			projectShareTokenService.getOrCreateShareToken(projectId, userDetails.getUsername()));
	}
}
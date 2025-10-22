package com.blockcloud.controller;

import com.blockcloud.dto.ResponseDto.ProjectViewResponseDto;
import com.blockcloud.dto.common.ResponseDto;
import com.blockcloud.service.ProjectViewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 프로젝트 공개 조회 API 컨트롤러 URL 공유를 위한 인증 없는 프로젝트 조회 기능을 제공합니다.
 */
@Tag(name = "Project View API", description = "프로젝트 공개 조회 및 URL 공유 관련 API")
@RestController
@RequestMapping("/api/view")
@RequiredArgsConstructor
public class ProjectViewController {

	private final ProjectViewService projectViewService;

	/**
	 * 프로젝트 ID와 공유 토큰으로 프로젝트 정보와 블록 아키텍처를 공개 조회합니다.
	 *
	 * @param projectId 조회할 프로젝트 ID
	 * @param token     공유 토큰
	 * @return 프로젝트 정보와 블록 아키텍처가 담긴 응답 객체
	 */
	@Operation(
		summary = "프로젝트 공개 조회",
		description = "프로젝트 ID와 공유 토큰으로 프로젝트 정보와 블록 아키텍처를 조회합니다. 유효한 공유 토큰이 필요합니다."
	)
	@GetMapping("/{projectId}")
	public ResponseDto<ProjectViewResponseDto> viewProject(
		@Parameter(description = "조회할 프로젝트 ID", required = true)
		@PathVariable Long projectId,
		@Parameter(description = "공유 토큰", required = true)
		@RequestParam String token) {
		return ResponseDto.ok(projectViewService.getProjectView(projectId, token));
	}
}
package com.blockcloud.controller;

import com.blockcloud.dto.RequestDto.ProjectRequestDto;
import com.blockcloud.dto.ResponseDto.ProjectResponseDto;
import com.blockcloud.dto.oauth.CustomUserDetails;
import com.blockcloud.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 프로젝트 관련 API 요청을 처리하는 컨트롤러입니다. 프로젝트 생성, 조회, 수정, 삭제 기능을 제공합니다.
 */
@Tag(name = "Project API", description = "프로젝트 생성, 조회, 수정, 삭제 관련 API")
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

	private final ProjectService projectService;

	public ProjectController(ProjectService projectService) {
		this.projectService = projectService;
	}

	/**
	 * 새로운 프로젝트를 생성합니다.
	 *
	 * @param dto            프로젝트 생성에 필요한 정보 (제목, 설명 등)
	 * @param authentication 인증 정보
	 * @return 생성된 프로젝트 정보가 담긴 응답 객체
	 */
	@Operation(
		summary = "프로젝트 생성",
		description = "새로운 프로젝트를 생성합니다. 요청 바디에 `name`, `description`을 포함해야 하며, JWT 토큰이 필요합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "프로젝트 생성 성공",
			content = @Content(schema = @Schema(implementation = ProjectResponseDto.class))),
		@ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
		@ApiResponse(responseCode = "401", description = "인증 실패 (JWT 필요)")
	})
	@PostMapping
	public ResponseEntity<ProjectResponseDto> create(
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "생성할 프로젝트 정보 (name: 프로젝트 이름, description: 설명)",
			required = true
		)
		@RequestBody ProjectRequestDto dto,
		Authentication authentication) {

		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		String email = userDetails.getUsername();

		ProjectResponseDto response = projectService.create(dto, email);
		return ResponseEntity.ok(response);
	}

	/**
	 * 프로젝트 목록을 페이징 처리(무한스크롤)하여 조회합니다.
	 *
	 * @param lastId 마지막으로 조회된 프로젝트 ID (첫 조회 시 null)
	 * @param size   한 번에 조회할 프로젝트 수 (기본값: 8)
	 * @return 프로젝트 목록과 다음 페이지 여부(`hasNext`)가 담긴 응답 객체
	 */
	@Operation(
		summary = "프로젝트 목록 조회",
		description = "프로젝트를 무한스크롤 방식으로 조회합니다. `lastId`를 넘기면 해당 ID보다 작은 프로젝트부터 조회하며, `size`로 가져올 개수를 지정할 수 있습니다."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "조회 성공",
			content = @Content(mediaType = "application/json")),
		@ApiResponse(responseCode = "401", description = "인증 실패 (JWT 필요)")
	})
	@GetMapping
	public ResponseEntity<Map<String, Object>> getProjects(
		@Parameter(description = "마지막으로 조회한 프로젝트 ID (첫 호출 시 생략 가능)")
		@RequestParam(required = false) Long lastId,
		@Parameter(description = "가져올 데이터 개수 (기본값 8)")
		@RequestParam(defaultValue = "8") int size) {

		return ResponseEntity.ok(projectService.findNext(lastId, size));
	}

	/**
	 * 기존 프로젝트의 정보를 수정합니다.
	 *
	 * @param projectId 수정할 프로젝트 ID
	 * @param dto       수정할 프로젝트 정보 (제목, 설명 등)
	 * @return 수정된 프로젝트 정보가 담긴 응답 객체
	 */
	@Operation(
		summary = "프로젝트 정보 수정",
		description = "기존 프로젝트의 제목과 설명을 수정합니다. JWT 인증 필요."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "수정 성공",
			content = @Content(schema = @Schema(implementation = ProjectResponseDto.class))),
		@ApiResponse(responseCode = "401", description = "인증 실패 (JWT 필요)"),
		@ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음")
	})
	@PutMapping("/{projectId}")
	public ResponseEntity<ProjectResponseDto> update(
		@Parameter(description = "수정할 프로젝트 ID", required = true)
		@PathVariable Long projectId,
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "수정할 프로젝트 정보 (name, description 포함)",
			required = true
		)
		@RequestBody ProjectRequestDto dto) {

		return ResponseEntity.ok(projectService.update(projectId, dto));
	}

	/**
	 * 프로젝트를 삭제합니다.
	 *
	 * @param projectId 삭제할 프로젝트 ID
	 * @return 삭제 성공 여부(`success`)와 메시지가 담긴 응답 객체
	 */
	@Operation(
		summary = "프로젝트 삭제",
		description = "프로젝트를 삭제합니다. JWT 인증 필요."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "삭제 성공",
			content = @Content(mediaType = "application/json")),
		@ApiResponse(responseCode = "401", description = "인증 실패 (JWT 필요)"),
		@ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음")
	})
	@DeleteMapping("/{projectId}")
	public ResponseEntity<Map<String, Object>> delete(
		@Parameter(description = "삭제할 프로젝트 ID", required = true)
		@PathVariable Long projectId) {

		boolean deleted = projectService.delete(projectId);
		return ResponseEntity.ok(Map.of(
			"success", deleted,
			"message", deleted ? "프로젝트를 성공적으로 삭제하였습니다." : "프로젝트를 찾을 수 없습니다."
		));
	}
}

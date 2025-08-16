package com.blockcloud.controller;

import com.blockcloud.dto.RequestDto.TerraformApplyRequestDto;
import com.blockcloud.dto.RequestDto.TerraformPlanRequestDto;
import com.blockcloud.dto.RequestDto.TerraformValidateRequestDto;
import com.blockcloud.dto.ResponseDto.DeploymentListResponseDto;
import com.blockcloud.dto.ResponseDto.DeploymentStatusResponseDto;
import com.blockcloud.dto.ResponseDto.TerraformApplyResponseDto;
import com.blockcloud.dto.ResponseDto.TerraformPlanResponseDto;
import com.blockcloud.dto.ResponseDto.TerraformValidateResponseDto;
import com.blockcloud.dto.common.ResponseDto;
import com.blockcloud.dto.oauth.CustomUserDetails;
import com.blockcloud.service.TerraformService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Terraform 관련 API 요청을 처리하는 컨트롤러입니다. Terraform 코드 검증, 배포, 배포 상태 조회 기능을 제공합니다.
 */
@Tag(name = "Terraform API", description = "Terraform 코드 검증, 배포, 배포 상태 조회 관련 API")
@RestController
@RequestMapping("/api/projects/{projectId}/terraform")
@RequiredArgsConstructor
public class TerraformController {

	private final TerraformService terraformService;

	/**
	 * Terraform 코드를 검증합니다.
	 *
	 * @param projectId 프로젝트 ID
	 * @param requestDto Terraform 코드 검증 요청
	 * @return 검증 결과가 담긴 응답 객체
	 */
	@Operation(
		summary = "Terraform 코드 검증",
		description = "Terraform 코드의 문법과 구성을 검증합니다. 유효성 검사 결과와 에러/경고 메시지를 반환합니다."
	)
	@PostMapping("/validate")
	public ResponseDto<TerraformValidateResponseDto> validateTerraform(
		@Parameter(description = "프로젝트 ID", required = true) @PathVariable Long projectId,
		@Valid @RequestBody TerraformValidateRequestDto requestDto) {
		return ResponseDto.ok(terraformService.validateTerraform(projectId, requestDto));
	}

	/**
	 * Terraform 코드의 변경 사항을 미리 확인합니다.
	 *
	 * @param projectId 프로젝트 ID
	 * @param requestDto Terraform plan 요청
	 * @return plan 결과가 담긴 응답 객체
	 */
	@Operation(
		summary = "Terraform 코드 Plan",
		description = "Terraform 코드를 실행했을 때 어떤 변경 사항이 발생할지 미리 확인합니다. 실제 배포는 하지 않습니다."
	)
	@PostMapping("/plan")
	public ResponseDto<TerraformPlanResponseDto> planTerraform(
		@Parameter(description = "프로젝트 ID", required = true) @PathVariable Long projectId,
		@Valid @RequestBody TerraformPlanRequestDto requestDto) {
		return ResponseDto.ok(terraformService.planTerraform(projectId, requestDto));
	}

	/**
	 * Terraform 코드를 적용하여 배포를 시작합니다.
	 *
	 * @param projectId 프로젝트 ID
	 * @param requestDto Terraform 배포 요청
	 * @param authentication 인증 정보
	 * @return 배포 시작 정보가 담긴 응답 객체
	 */
	@Operation(
		summary = "Terraform 코드 배포",
		description = "Terraform 코드를 실제 클라우드 환경에 적용하여 인프라를 배포합니다. 배포는 비동기로 실행되며, 배포 ID를 반환합니다."
	)
	@PostMapping("/apply")
	public ResponseDto<TerraformApplyResponseDto> applyTerraform(
		@Parameter(description = "프로젝트 ID", required = true) @PathVariable Long projectId,
		@Valid @RequestBody TerraformApplyRequestDto requestDto,
		Authentication authentication) {
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		return ResponseDto.ok(terraformService.applyTerraform(projectId, requestDto, userDetails.getUsername()));
	}

	/**
	 * 특정 배포의 상태를 조회합니다.
	 *
	 * @param projectId 프로젝트 ID
	 * @param deploymentId 배포 ID
	 * @return 배포 상태 정보가 담긴 응답 객체
	 */
	@Operation(
		summary = "배포 상태 조회",
		description = "특정 배포의 현재 상태를 조회합니다. PENDING, RUNNING, SUCCESS, FAILED 상태와 상세 정보를 반환합니다."
	)
	@GetMapping("/deployments/{deploymentId}")
	public ResponseDto<DeploymentStatusResponseDto> getDeploymentStatus(
		@Parameter(description = "프로젝트 ID", required = true) @PathVariable Long projectId,
		@Parameter(description = "배포 ID", required = true) @PathVariable Long deploymentId) {
		return ResponseDto.ok(terraformService.getDeploymentStatus(projectId, deploymentId));
	}

	/**
	 * 프로젝트의 배포 이력을 조회합니다.
	 *
	 * @param projectId 프로젝트 ID
	 * @param lastId 마지막으로 조회된 배포 ID (첫 조회 시 null)
	 * @param size 한 번에 조회할 배포 수 (기본값: 10)
	 * @return 배포 이력 목록이 담긴 응답 객체
	 */
	@Operation(
		summary = "배포 이력 조회",
		description = "프로젝트의 배포 이력을 무한스크롤 방식으로 조회합니다. `lastId`를 넘기면 해당 ID보다 작은 배포부터 조회하며, `size`로 가져올 개수를 지정할 수 있습니다."
	)
	@GetMapping("/deployments")
	public ResponseDto<DeploymentListResponseDto> getDeploymentHistory(
		@Parameter(description = "프로젝트 ID", required = true) @PathVariable Long projectId,
		@Parameter(description = "마지막으로 조회한 배포 ID (첫 호출 시 생략 가능)")
		@RequestParam(required = false) Long lastId,
		@Parameter(description = "가져올 데이터 개수 (기본값 10)")
		@RequestParam(defaultValue = "10") int size) {
		return ResponseDto.ok(terraformService.getDeploymentHistory(projectId, lastId, size));
	}
}

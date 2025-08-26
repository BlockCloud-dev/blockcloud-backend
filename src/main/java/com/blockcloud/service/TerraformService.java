package com.blockcloud.service;

import com.blockcloud.domain.deployment.Deployment;
import com.blockcloud.domain.deployment.DeploymentRepository;
import com.blockcloud.domain.deployment.DeploymentStatus;
import com.blockcloud.domain.project.Project;
import com.blockcloud.domain.project.ProjectRepository;
import com.blockcloud.dto.RequestDto.TerraformApplyRequestDto;

import com.blockcloud.dto.RequestDto.TerraformPlanRequestDto;
import com.blockcloud.dto.RequestDto.TerraformValidateRequestDto;
import com.blockcloud.dto.ResponseDto.DeploymentListResponseDto;
import com.blockcloud.dto.ResponseDto.DeploymentStatusResponseDto;
import com.blockcloud.dto.ResponseDto.TerraformApplyResponseDto;
import com.blockcloud.dto.ResponseDto.TerraformDestroyResponseDto;
import com.blockcloud.dto.ResponseDto.TerraformPlanResponseDto;
import com.blockcloud.dto.ResponseDto.TerraformValidateResponseDto;
import com.blockcloud.exception.CommonException;
import com.blockcloud.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TerraformService {

	private final ProjectRepository projectRepository;
	private final DeploymentRepository deploymentRepository;
	private final TerraformExecutor terraformExecutor;

	/**
	 * Terraform 코드를 검증합니다.
	 */
	public TerraformValidateResponseDto validateTerraform(TerraformValidateRequestDto requestDto) {
		TerraformExecutor.TerraformExecutionResult result = terraformExecutor.validate(requestDto.getTerraformCode());
		
		return TerraformValidateResponseDto.builder()
			.isValid(result.isSuccess())
			.output(result.getOutput())
			.errorMessage(result.getError())
			.build();
	}

	/**
	 * Terraform 코드의 변경 사항을 미리 확인합니다.
	 */
	public TerraformPlanResponseDto planTerraform(Long projectId, TerraformPlanRequestDto requestDto) {
		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_PROJECT));

		TerraformExecutor.TerraformExecutionResult result = terraformExecutor.plan(requestDto.getTerraformCode());
		
		// Terraform plan의 exit code: 0=성공, 1=에러, 2=변경사항 있음
		boolean hasChanges = result.getExitCode() == 2;
		
		return TerraformPlanResponseDto.builder()
			.hasChanges(hasChanges)
			.planOutput(result.getOutput())
			.errorMessage(result.getError())
			.build();
	}

	@Transactional
	public TerraformApplyResponseDto applyTerraform(Long projectId, TerraformApplyRequestDto requestDto, String username) {
		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_PROJECT));

		// 배포 이력 생성
		Deployment deployment = Deployment.builder()
			.project(project)
			.status(DeploymentStatus.PENDING)
			.message("배포 대기 중")
			.terraformCode(requestDto.getTerraformCode())
			.startedAt(LocalDateTime.now())
			.build();

		Deployment savedDeployment = deploymentRepository.save(deployment);

		// 비동기로 배포 실행
		CompletableFuture.runAsync(() -> {
			try {
				executeTerraformApply(savedDeployment.getId(), projectId, requestDto.getTerraformCode());
			} catch (Exception e) {
				log.error("Terraform apply failed for deployment {}: {}", savedDeployment.getId(), e.getMessage());
				updateDeploymentStatus(savedDeployment.getId(), DeploymentStatus.FAILED, "배포 실패: " + e.getMessage());
			}
		});

		return TerraformApplyResponseDto.builder()
			.deploymentId(savedDeployment.getId())
			.status("PENDING")
			.message("배포가 시작되었습니다.")
			.startedAt(savedDeployment.getStartedAt())
			.build();
	}

	@Transactional
	public TerraformDestroyResponseDto destroyTerraformByDeployment(Long projectId, Long deploymentId, String username) {
		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_PROJECT));

		Deployment deployment = deploymentRepository.findById(deploymentId)
			.orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_DEPLOYMENT));

		// 프로젝트에 속한 배포인지 확인
		if (!deployment.getProject().getId().equals(projectId)) {
			throw new CommonException(ErrorCode.ACCESS_DENIED);
		}

		// 삭제용 배포 이력 생성
		Deployment destroyDeployment = Deployment.builder()
			.project(project)
			.status(DeploymentStatus.PENDING)
			.message("인프라 삭제 대기 중")
			.terraformCode(deployment.getTerraformCode())
			.startedAt(LocalDateTime.now())
			.build();

		Deployment savedDestroyDeployment = deploymentRepository.save(destroyDeployment);

		// 비동기로 삭제 실행
		CompletableFuture.runAsync(() -> {
			try {
				executeTerraformDestroy(savedDestroyDeployment.getId(), projectId, deployment.getTerraformCode());
			} catch (Exception e) {
				log.error("Terraform destroy failed for deployment {}: {}", savedDestroyDeployment.getId(), e.getMessage());
				updateDeploymentStatus(savedDestroyDeployment.getId(), DeploymentStatus.FAILED, "삭제 실패: " + e.getMessage());
			}
		});

		return TerraformDestroyResponseDto.builder()
			.deploymentId(savedDestroyDeployment.getId())
			.status("PENDING")
			.message("인프라 삭제가 시작되었습니다.")
			.startedAt(savedDestroyDeployment.getStartedAt())
			.build();
	}

	public DeploymentStatusResponseDto getDeploymentStatus(Long projectId, Long deploymentId) {
		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_PROJECT));

		Deployment deployment = deploymentRepository.findById(deploymentId)
			.orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_DEPLOYMENT));

		// 프로젝트에 속한 배포인지 확인
		if (!deployment.getProject().getId().equals(projectId)) {
			throw new CommonException(ErrorCode.ACCESS_DENIED);
		}

		return DeploymentStatusResponseDto.builder()
			.deploymentId(deployment.getId())
			.status(deployment.getStatus().name())
			.message(deployment.getMessage())
			.startedAt(deployment.getStartedAt())
			.completedAt(deployment.getCompletedAt())
			.output(deployment.getOutput())
			.build();
	}

	public DeploymentListResponseDto getDeploymentHistory(Long projectId, Long lastId, int size) {
		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_PROJECT));

		PageRequest pageRequest = PageRequest.of(0, size + 1);
		List<Deployment> deployments = deploymentRepository.findByProjectOrderByCreatedAtDesc(project, pageRequest);

		boolean hasNext = deployments.size() > size;
		if (hasNext) {
			deployments = deployments.subList(0, size);
		}

		List<DeploymentStatusResponseDto> deploymentDtos = deployments.stream()
			.map(deployment -> DeploymentStatusResponseDto.builder()
				.deploymentId(deployment.getId())
				.status(deployment.getStatus().name())
				.message(deployment.getMessage())
				.startedAt(deployment.getStartedAt())
				.completedAt(deployment.getCompletedAt())
				.output(deployment.getOutput())
				.build())
			.toList();

		return DeploymentListResponseDto.builder()
			.deployments(deploymentDtos)
			.hasNext(hasNext)
			.build();
	}

	private void executeTerraformApply(Long deploymentId, Long projectId, String terraformCode) {
		try {
			// 상태를 RUNNING으로 업데이트
			updateDeploymentStatus(deploymentId, DeploymentStatus.RUNNING, "배포 실행 중");

			// Terraform apply 실행
			TerraformExecutor.TerraformExecutionResult result = terraformExecutor.apply(terraformCode);
			
			if (result.isSuccess()) {
				updateDeploymentStatus(deploymentId, DeploymentStatus.SUCCESS, "배포 성공");
				updateDeploymentOutput(deploymentId, result.getOutput());
			} else {
				updateDeploymentStatus(deploymentId, DeploymentStatus.FAILED, "배포 실패: " + result.getError());
				updateDeploymentOutput(deploymentId, result.getError());
			}
			
		} catch (Exception e) {
			updateDeploymentStatus(deploymentId, DeploymentStatus.FAILED, "배포 중 오류 발생: " + e.getMessage());
		}
	}

	private void executeTerraformDestroy(Long deploymentId, Long projectId, String terraformCode) {
		try {
			// 상태를 RUNNING으로 업데이트
			updateDeploymentStatus(deploymentId, DeploymentStatus.RUNNING, "인프라 삭제 실행 중");

			// Terraform destroy 실행
			TerraformExecutor.TerraformExecutionResult result = terraformExecutor.destroy(terraformCode);
			
			if (result.isSuccess()) {
				updateDeploymentStatus(deploymentId, DeploymentStatus.SUCCESS, "인프라 삭제 성공");
				updateDeploymentOutput(deploymentId, result.getOutput());
			} else {
				updateDeploymentStatus(deploymentId, DeploymentStatus.FAILED, "인프라 삭제 실패: " + result.getError());
				updateDeploymentOutput(deploymentId, result.getError());
			}
			
		} catch (Exception e) {
			updateDeploymentStatus(deploymentId, DeploymentStatus.FAILED, "인프라 삭제 중 오류 발생: " + e.getMessage());
		}
	}

	@Transactional
	protected void updateDeploymentStatus(Long deploymentId, DeploymentStatus status, String message) {
		Deployment deployment = deploymentRepository.findById(deploymentId)
			.orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_DEPLOYMENT));
		deployment.updateStatus(status, message);
	}

	@Transactional
	protected void updateDeploymentOutput(Long deploymentId, String output) {
		Deployment deployment = deploymentRepository.findById(deploymentId)
			.orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_DEPLOYMENT));
		deployment.setOutput(output);
	}
}

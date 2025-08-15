package com.blockcloud.service;

import com.blockcloud.domain.deployment.Deployment;
import com.blockcloud.domain.deployment.DeploymentRepository;
import com.blockcloud.domain.deployment.DeploymentStatus;
import com.blockcloud.domain.project.Project;
import com.blockcloud.domain.project.ProjectRepository;
import com.blockcloud.dto.RequestDto.TerraformApplyRequestDto;
import com.blockcloud.dto.RequestDto.TerraformValidateRequestDto;
import com.blockcloud.dto.ResponseDto.DeploymentListResponseDto;
import com.blockcloud.dto.ResponseDto.DeploymentStatusResponseDto;
import com.blockcloud.dto.ResponseDto.TerraformApplyResponseDto;
import com.blockcloud.dto.ResponseDto.TerraformValidateResponseDto;
import com.blockcloud.exception.CommonException;
import com.blockcloud.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TerraformService {

	private final ProjectRepository projectRepository;
	private final DeploymentRepository deploymentRepository;

	/**
	 * Terraform 코드를 검증합니다.
	 */
	public TerraformValidateResponseDto validateTerraform(Long projectId, TerraformValidateRequestDto requestDto) {
		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_PROJECT));

		try {
			// 임시 디렉토리 생성
			String tempDir = createTempDirectory(projectId);
			String terraformFile = tempDir + "/main.tf";
			
			// Terraform 파일 생성
			writeTerraformFile(terraformFile, requestDto.getTerraformCode());
			
			// Terraform validate 실행
			ProcessBuilder processBuilder = new ProcessBuilder("terraform", "validate");
			processBuilder.directory(new File(tempDir));
			
			Process process = processBuilder.start();
			int exitCode = process.waitFor();
			
			List<String> errors = new ArrayList<>();
			List<String> warnings = new ArrayList<>();
			
			if (exitCode != 0) {
				// 에러 출력 읽기
				String errorOutput = new String(process.getErrorStream().readAllBytes());
				errors.add(errorOutput);
			}
			
			// 임시 디렉토리 정리
			cleanupTempDirectory(tempDir);
			
			return TerraformValidateResponseDto.builder()
				.isValid(exitCode == 0)
				.errors(errors)
				.warnings(warnings)
				.build();
				
		} catch (Exception e) {
			log.error("Terraform validation failed for project {}: {}", projectId, e.getMessage());
			return TerraformValidateResponseDto.builder()
				.isValid(false)
				.errors(List.of("Terraform 검증 중 오류가 발생했습니다: " + e.getMessage()))
				.warnings(new ArrayList<>())
				.build();
		}
	}

	/**
	 * Terraform 코드를 적용하여 배포를 시작합니다.
	 */
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

	/**
	 * 배포 상태를 조회합니다.
	 */
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

	/**
	 * 프로젝트의 배포 이력을 조회합니다.
	 */
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

	// Private helper methods

	private String createTempDirectory(Long projectId) throws IOException {
		String tempDir = System.getProperty("java.io.tmpdir") + "/terraform-" + projectId + "-" + UUID.randomUUID();
		Files.createDirectories(Paths.get(tempDir));
		return tempDir;
	}

	private void writeTerraformFile(String filePath, String terraformCode) throws IOException {
		try (FileWriter writer = new FileWriter(filePath)) {
			writer.write(terraformCode);
		}
	}

	private void cleanupTempDirectory(String tempDir) {
		try {
			Path path = Paths.get(tempDir);
			Files.walk(path)
				.sorted((a, b) -> b.compareTo(a))
				.forEach(p -> {
					try {
						Files.delete(p);
					} catch (IOException e) {
						log.warn("Failed to delete temp file: {}", p);
					}
				});
		} catch (IOException e) {
			log.warn("Failed to cleanup temp directory: {}", tempDir);
		}
	}

	private void executeTerraformApply(Long deploymentId, Long projectId, String terraformCode) {
		try {
			// 상태를 RUNNING으로 업데이트
			updateDeploymentStatus(deploymentId, DeploymentStatus.RUNNING, "배포 실행 중");

			// 임시 디렉토리 생성
			String tempDir = createTempDirectory(projectId);
			String terraformFile = tempDir + "/main.tf";
			
			// Terraform 파일 생성
			writeTerraformFile(terraformFile, terraformCode);
			
			// Terraform init 실행
			ProcessBuilder initBuilder = new ProcessBuilder("terraform", "init");
			initBuilder.directory(new File(tempDir));
			Process initProcess = initBuilder.start();
			int initExitCode = initProcess.waitFor();
			
			if (initExitCode != 0) {
				String errorOutput = new String(initProcess.getErrorStream().readAllBytes());
				updateDeploymentStatus(deploymentId, DeploymentStatus.FAILED, "Terraform 초기화 실패: " + errorOutput);
				cleanupTempDirectory(tempDir);
				return;
			}
			
			// Terraform apply 실행
			ProcessBuilder applyBuilder = new ProcessBuilder("terraform", "apply", "-auto-approve");
			applyBuilder.directory(new File(tempDir));
			Process applyProcess = applyBuilder.start();
			int applyExitCode = applyProcess.waitFor();
			
			String output = new String(applyProcess.getInputStream().readAllBytes());
			String errorOutput = new String(applyProcess.getErrorStream().readAllBytes());
			
			if (applyExitCode == 0) {
				updateDeploymentStatus(deploymentId, DeploymentStatus.SUCCESS, "배포 성공");
				updateDeploymentOutput(deploymentId, output);
			} else {
				updateDeploymentStatus(deploymentId, DeploymentStatus.FAILED, "배포 실패: " + errorOutput);
				updateDeploymentOutput(deploymentId, errorOutput);
			}
			
			// 임시 디렉토리 정리
			cleanupTempDirectory(tempDir);
			
		} catch (Exception e) {
			updateDeploymentStatus(deploymentId, DeploymentStatus.FAILED, "배포 중 오류 발생: " + e.getMessage());
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

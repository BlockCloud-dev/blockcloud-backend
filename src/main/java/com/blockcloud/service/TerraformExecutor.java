package com.blockcloud.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class TerraformExecutor {

	private static final String BASE_DIR = "/tmp/terraform/";

	/**
	 * Terraform 명령어를 실행합니다.
	 *
	 * @param terraformCode Terraform 코드
	 * @param command 실행할 명령어 (init, validate, plan, apply 등)
	 * @return 실행 결과
	 */
	public TerraformExecutionResult executeCommand(String terraformCode, String command) {
		String projectId = UUID.randomUUID().toString();
		Path workDir = Paths.get(BASE_DIR, projectId);

		try {
			// 작업 디렉토리 생성
			Files.createDirectories(workDir);

			// Terraform 파일 생성
			Path terraformFile = workDir.resolve("main.tf");
			Files.writeString(terraformFile, terraformCode);

			// Terraform init 실행
			ProcessBuilder initBuilder = new ProcessBuilder("terraform", "init", "-input=false")
				.directory(workDir.toFile());
			Process initProcess = initBuilder.start();
			int initExitCode = initProcess.waitFor();

			if (initExitCode != 0) {
				String initError = new String(initProcess.getErrorStream().readAllBytes());
				return TerraformExecutionResult.builder()
					.success(false)
					.exitCode(initExitCode)
					.output("")
					.error("Terraform 초기화 실패: " + initError)
					.build();
			}

			// 요청된 명령어 실행
			List<String> cmd = new ArrayList<>(List.of("terraform"));
			cmd.addAll(List.of(command.split(" ")));
			
			ProcessBuilder builder = new ProcessBuilder(cmd)
				.directory(workDir.toFile());
			Process process = builder.start();

			// 출력과 에러 스트림 읽기
			String output = new String(process.getInputStream().readAllBytes());
			String error = new String(process.getErrorStream().readAllBytes());
			int exitCode = process.waitFor();

			// 작업 디렉토리 정리
			cleanupDirectory(workDir);

			return TerraformExecutionResult.builder()
				.success(exitCode == 0)
				.exitCode(exitCode)
				.output(output)
				.error(error)
				.build();

		} catch (Exception e) {
			log.error("Terraform 명령어 실행 중 오류 발생: {}", e.getMessage(), e);
			
			// 작업 디렉토리 정리
			try {
				cleanupDirectory(workDir);
			} catch (Exception cleanupException) {
				log.warn("작업 디렉토리 정리 중 오류: {}", cleanupException.getMessage());
			}

			return TerraformExecutionResult.builder()
				.success(false)
				.exitCode(-1)
				.output("")
				.error("Terraform 실행 중 오류 발생: " + e.getMessage())
				.build();
		}
	}

	/**
	 * Terraform plan을 실행하여 변경 사항을 미리 확인합니다.
	 *
	 * @param terraformCode Terraform 코드
	 * @return plan 실행 결과
	 */
	public TerraformExecutionResult plan(String terraformCode) {
		return executeCommand(terraformCode, "plan -detailed-exitcode");
	}

	/**
	 * Terraform validate를 실행하여 코드를 검증합니다.
	 *
	 * @param terraformCode Terraform 코드
	 * @return validate 실행 결과
	 */
	public TerraformExecutionResult validate(String terraformCode) {
		return executeCommand(terraformCode, "validate");
	}

	/**
	 * Terraform apply를 실행하여 인프라를 배포합니다.
	 *
	 * @param terraformCode Terraform 코드
	 * @return apply 실행 결과
	 */
	public TerraformExecutionResult apply(String terraformCode) {
		return executeCommand(terraformCode, "apply -auto-approve");
	}

	/**
	 * 작업 디렉토리를 정리합니다.
	 */
	private void cleanupDirectory(Path directory) {
		try {
			if (Files.exists(directory)) {
				Files.walk(directory)
					.sorted((a, b) -> b.compareTo(a)) // 하위 디렉토리부터 삭제
					.forEach(path -> {
						try {
							Files.delete(path);
						} catch (IOException e) {
							log.warn("파일 삭제 실패: {}", path);
						}
					});
			}
		} catch (IOException e) {
			log.warn("디렉토리 정리 중 오류: {}", e.getMessage());
		}
	}

	/**
	 * Terraform 실행 결과를 담는 내부 클래스
	 */
	public static class TerraformExecutionResult {
		private final boolean success;
		private final int exitCode;
		private final String output;
		private final String error;

		@lombok.Builder
		public TerraformExecutionResult(boolean success, int exitCode, String output, String error) {
			this.success = success;
			this.exitCode = exitCode;
			this.output = output;
			this.error = error;
		}

		public boolean isSuccess() {
			return success;
		}

		public int getExitCode() {
			return exitCode;
		}

		public String getOutput() {
			return output;
		}

		public String getError() {
			return error;
		}
	}
}

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
	private static final String TERRAFORM_PATH = "/usr/local/bin/terraform"; // Terraform 절대 경로

	public TerraformExecutionResult executeCommand(String terraformCode, String command) {
		String projectId = UUID.randomUUID().toString();
		Path workDir = Paths.get(BASE_DIR, projectId);

		try {
			Files.createDirectories(workDir);

			Path terraformFile = workDir.resolve("main.tf");
			Files.writeString(terraformFile, terraformCode);

			ProcessBuilder initBuilder = new ProcessBuilder(TERRAFORM_PATH, "init", "-input=false")
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

			List<String> cmd = new ArrayList<>(List.of(TERRAFORM_PATH));
			cmd.addAll(List.of(command.split(" ")));

			ProcessBuilder builder = new ProcessBuilder(cmd).directory(workDir.toFile());
			Process process = builder.start();

			String output = new String(process.getInputStream().readAllBytes());
			String error = new String(process.getErrorStream().readAllBytes());
			int exitCode = process.waitFor();

			cleanupDirectory(workDir);

			return TerraformExecutionResult.builder()
				.success(exitCode == 0)
				.exitCode(exitCode)
				.output(output)
				.error(error)
				.build();

		} catch (Exception e) {
			log.error("Terraform 명령어 실행 중 오류 발생: {}", e.getMessage(), e);
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

	public TerraformExecutionResult plan(String terraformCode) {
		return executeCommand(terraformCode, "plan -detailed-exitcode");
	}

	public TerraformExecutionResult validate(String terraformCode) {
		return executeCommand(terraformCode, "validate");
	}

	public TerraformExecutionResult apply(String terraformCode) {
		return executeCommand(terraformCode, "apply -auto-approve");
	}

	public TerraformExecutionResult destroy(String terraformCode) {
		return executeCommand(terraformCode, "destroy -auto-approve");
	}

	public TerraformExecutionResult runFullWorkflow(String terraformCode) {
		TerraformExecutionResult validateResult = validate(terraformCode);
		if (!validateResult.isSuccess()) return validateResult;

		TerraformExecutionResult planResult = plan(terraformCode);
		if (!planResult.isSuccess()) return planResult;

		return apply(terraformCode);
	}

	private void cleanupDirectory(Path directory) {
		try {
			if (Files.exists(directory)) {
				Files.walk(directory)
					.sorted((a, b) -> b.compareTo(a))
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

		public boolean isSuccess() { return success; }
		public int getExitCode() { return exitCode; }
		public String getOutput() { return output; }
		public String getError() { return error; }
	}
}

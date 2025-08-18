package com.blockcloud.dto.ResponseDto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DeploymentStatusResponseDto {

	private Long deploymentId;
	private String status; // PENDING, RUNNING, SUCCESS, FAILED
	private String message;
	private LocalDateTime startedAt;
	private LocalDateTime completedAt;
	private String output;
}

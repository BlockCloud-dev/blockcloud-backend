package com.blockcloud.dto.ResponseDto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TerraformDestroyResponseDto {

	private Long deploymentId;
	private String status;
	private String message;
	private LocalDateTime startedAt;
	private String output;
	private String errorMessage;
}

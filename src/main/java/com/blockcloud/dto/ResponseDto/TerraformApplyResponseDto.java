package com.blockcloud.dto.ResponseDto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TerraformApplyResponseDto {

	private Long deploymentId;
	private String status;
	private String message;
	private LocalDateTime startedAt;
}

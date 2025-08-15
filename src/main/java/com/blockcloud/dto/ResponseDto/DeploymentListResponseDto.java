package com.blockcloud.dto.ResponseDto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DeploymentListResponseDto {

	private List<DeploymentStatusResponseDto> deployments;
	private boolean hasNext;
}

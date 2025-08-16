package com.blockcloud.dto.ResponseDto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TerraformPlanResponseDto {

	private boolean hasChanges;
	private String planOutput;
	private String errorMessage;
}

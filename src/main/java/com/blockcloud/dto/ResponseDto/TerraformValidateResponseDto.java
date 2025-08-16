package com.blockcloud.dto.ResponseDto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TerraformValidateResponseDto {

	private boolean isValid;
	private String output;
	private String errorMessage;
}

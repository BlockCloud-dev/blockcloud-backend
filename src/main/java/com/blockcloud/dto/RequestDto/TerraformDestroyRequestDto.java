package com.blockcloud.dto.RequestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TerraformDestroyRequestDto {

	@NotBlank(message = "Terraform 코드는 필수입니다.")
	private String terraformCode;
}

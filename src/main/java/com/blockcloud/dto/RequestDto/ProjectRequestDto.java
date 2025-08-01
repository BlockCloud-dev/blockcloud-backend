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
public class ProjectRequestDto {

	@NotBlank(message = "프로젝트 이름은 필수입니다.")
	private String name;
	private String description;
}
package com.blockcloud.dto.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 블록 저장 요청 DTO
 * 블록 정보를 포함하여 저장 요청을 처리하기 위한 DTO 클래스입니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockSaveRequestDto {

	@NotNull(message = "블록 정보는 필수입니다.")
	private Object blocks;
}
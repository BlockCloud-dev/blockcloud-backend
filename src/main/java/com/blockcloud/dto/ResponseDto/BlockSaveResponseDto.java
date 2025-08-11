package com.blockcloud.dto.ResponseDto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 블록 저장 응답 DTO
 */
@Getter
@Builder
public class BlockSaveResponseDto {

	private Long projectId;
	private String architectureName;
	private LocalDateTime updatedAt;
}

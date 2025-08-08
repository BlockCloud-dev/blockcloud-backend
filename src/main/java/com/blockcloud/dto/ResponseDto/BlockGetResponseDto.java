package com.blockcloud.dto.ResponseDto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 블록 조회 성공 시 'data' 필드에 담길 응답 DTO
 */
@Getter
@Builder
public class BlockGetResponseDto {

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private Object blocks;
}
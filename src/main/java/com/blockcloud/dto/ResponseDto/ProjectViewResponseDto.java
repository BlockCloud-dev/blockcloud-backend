package com.blockcloud.dto.ResponseDto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 프로젝트 공개 조회용 응답 DTO
 * URL 공유를 위한 프로젝트 정보와 블록 아키텍처 정보를 포함합니다.
 */
@Getter
@Builder
public class ProjectViewResponseDto {

	private Long id;
	private String name;
	private String description;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private Object blocks; // 블록 아키텍처 정보 (JSON 형태)
}


package com.blockcloud.dto.ResponseDto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 프로젝트 응답 DTO 프로젝트의 ID, 이름, 설명, 생성 및 수정 시간을 포함합니다.
 */
@Getter
@Builder
public class ProjectResponseDto {

	private Long id;
	private String name;
	private String description;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
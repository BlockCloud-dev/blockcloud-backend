package com.blockcloud.dto.ResponseDto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * 프로젝트 목록 조회 시 'data' 필드에 담길 응답 DTO.
 */
@Getter
@Builder
public class ProjectListResponseDto {

	private List<ProjectResponseDto> projects;
	private boolean hasNext;
}

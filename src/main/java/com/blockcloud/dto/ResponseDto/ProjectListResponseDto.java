package com.blockcloud.dto.ResponseDto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectListResponseDto {

	private boolean success;
	private List<ProjectResponseDto.ProjectInfo> projects;
	private boolean hasNext;
}
package com.blockcloud.dto.ResponseDto;

import com.blockcloud.dto.ResponseDto.ProjectResponseDto.ProjectInfo;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectListResponseDto {
	private List<ProjectInfo> projects;
	private boolean hasNext;
}
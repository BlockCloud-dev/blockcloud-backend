package com.blockcloud.dto.ResponseDto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectResponseDto {

	private boolean success;
	private ProjectInfo project;

	@Getter
	@Builder
	public static class ProjectInfo {

		private Long id;
		private String name;
		private String description;
		private LocalDateTime createdAt;
		private LocalDateTime updatedAt;
	}
}
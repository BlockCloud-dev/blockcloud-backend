package com.blockcloud.dto.ResponseDto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BlockSaveResponseDto {

	private boolean success;
	private String message;
	private BlockInfo data;

	@Getter
	@Builder
	public static class BlockInfo {

		private Long projectId;
		private String architectureName;
		private LocalDateTime updatedAt;
	}
}
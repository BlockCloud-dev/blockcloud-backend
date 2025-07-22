package com.blockcloud.dto.RequestDto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockSaveRequestDto {

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private Object blocks;
}
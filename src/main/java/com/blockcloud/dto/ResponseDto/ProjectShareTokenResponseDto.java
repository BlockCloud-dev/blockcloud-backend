package com.blockcloud.dto.ResponseDto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 프로젝트 공유 토큰 응답 DTO
 */
@Getter
@Builder
public class ProjectShareTokenResponseDto {

	private Long projectId;
	private String token;
	private String shareUrl;
	private LocalDateTime createdAt;
	private LocalDateTime expiresAt;
}

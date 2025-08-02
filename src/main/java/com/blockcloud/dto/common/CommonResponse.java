package com.blockcloud.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * API 응답의 공통 형식을 제공하는 DTO입니다.
 * 주로 성공 여부와 메시지를 담아 반환할 때 사용됩니다.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommonResponse {
	private boolean success;
	private String message;
}

package com.blockcloud.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

	// 400 - JSON 파싱 오류, 잘못된 블록 데이터
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> handleInvalidJson() {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
			Map.of(
				"success", false,
				"errorCode", "INVALID_BLOCKS",
				"message", "블록 데이터가 유효하지 않습니다"
			)
		);
	}

	// 403 - 권한 부족
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Map<String, Object>> handleForbidden() {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
			Map.of(
				"success", false,
				"errorCode", "FORBIDDEN",
				"message", "프로젝트에 접근할 수 없습니다"
			)
		);
	}

	// 404 - 프로젝트 없음
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleProjectNotFound() {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
			Map.of(
				"success", false,
				"errorCode", "PROJECT_NOT_FOUND",
				"message", "해당 프로젝트를 찾을 수 없습니다"
			)
		);
	}
}
package com.blockcloud.config.handler;

import com.blockcloud.dto.common.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 핸들러
 * 애플리케이션 전반에서 발생하는 예외를 중앙 집중식으로 처리합니다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 존재하지 않는 리소스에 접근했을 때 발생하는 예외를 처리합니다.
	 * HTTP 상태 코드 404 (Not Found)와 함께 사용자 친화적인 메시지를 반환합니다.
	 * @param e 발생한 예외
	 * @return 404 응답과 에러 메시지
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<CommonResponse> handleIllegalArgumentException(IllegalArgumentException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(new CommonResponse(false, e.getMessage()));
	}

	/**
	 * 접근 권한이 없는 리소스에 접근했을 때 발생하는 예외를 처리합니다.
	 * HTTP 상태 코드 403 (Forbidden)과 함께 사용자 친화적인 메시지를 반환합니다.
	 * @param e 발생한 예외
	 * @return 403 응답과 에러 메시지
	 */
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<CommonResponse> handleAccessDeniedException(AccessDeniedException e) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
			.body(new CommonResponse(false, e.getMessage()));
	}

	/**
	 * DTO 유효성 검증에 실패했을 때 발생하는 예외를 처리합니다.
	 * HTTP 상태 코드 400 (Bad Request)과 함께 첫 번째 유효성 검증 에러 메시지를 반환합니다.
	 * @param e 발생한 예외
	 * @return 400 응답과 에러 메시지
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<CommonResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		String errorMessage = e.getBindingResult().getFieldError().getDefaultMessage();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(new CommonResponse(false, errorMessage));
	}
}
package com.blockcloud.dto.common;

import com.blockcloud.exception.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.Getter;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.HashMap;
import java.util.Map;

/**
 * 유효성 검증 실패 시 발생하는 필드 오류 정보를 담는 DTO.
 */

@Getter
public non-sealed class ArgumentNotValidExceptionDto extends ExceptionDto {

    @JsonProperty("fields")
    private final Map<String, String> fields;

    // MethodArgumentNotValidException: @RequestBody 검증 실패 시
    public ArgumentNotValidExceptionDto(MethodArgumentNotValidException exception) {
        super(ErrorCode.INVALID_ARGUMENT);

        this.fields = new HashMap<>();
        exception.getBindingResult()
                .getAllErrors().forEach(e -> this.fields.put(toSnakeCase(((FieldError) e).getField()), e.getDefaultMessage()));
    }

    // ConstraintViolationException: @RequestParam, @PathVariable 등에서 검증 실패 시
    public ArgumentNotValidExceptionDto(ConstraintViolationException exception) {
        super(ErrorCode.INVALID_ARGUMENT);

        this.fields = new HashMap<>();

        for (ConstraintViolation<?> constraintViolation : exception.getConstraintViolations()) {
            fields.put(toSnakeCase(constraintViolation.getPropertyPath().toString()), constraintViolation.getMessage());
        }
    }

    // HandlerMethodValidationException: 스프링 6 이후 지원되는 파라미터 검증 실패 시
    public ArgumentNotValidExceptionDto(HandlerMethodValidationException exception) {
        super(ErrorCode.INVALID_ARGUMENT);

        this.fields = new HashMap<>();

        for (ParameterValidationResult result : exception.getAllValidationResults()) {
            String argumentName = result.getMethodParameter().getParameterName();

            if (argumentName == null) {
                continue;
            }

            fields.put(toSnakeCase(argumentName), result.getResolvableErrors().get(0).getDefaultMessage());
        }
    }

    private String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }
}

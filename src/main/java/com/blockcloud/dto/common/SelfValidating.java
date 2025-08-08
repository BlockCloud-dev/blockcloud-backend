package com.blockcloud.dto.common;

import com.blockcloud.exception.CommonException;
import com.blockcloud.exception.error.ErrorCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * 유효성 검사 기반의 추상 클래스
 * 해당 상속한 클래스는 객체 생성 시점에 Bean Validation (@NotNull, @Size 등)을 자동 수행함.
 * @param <T> 유효성 검사를 수행할 클래스 타입
 */
@Slf4j
public abstract class SelfValidating<T> {

    private final Validator validator;

    public SelfValidating() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * 현재 객체에 대해 Bean Validation 규칙을 검사함.
     * - 유효성 위반이 발견되면 로그를 출력하고 CommonException을 던짐.
     * - 생성자나 빌더 내부에서 호출되어 자동 검증 역할을 함.
     */
    protected void validateSelf() {
        Set<ConstraintViolation<T>> violations = validator.validate((T) this);
        if (!violations.isEmpty()) {
            log.error("Validation error occurred: {}", violations);
            throw new CommonException(ErrorCode.INTERNAL_DATA_ERROR);
        }
    }
}

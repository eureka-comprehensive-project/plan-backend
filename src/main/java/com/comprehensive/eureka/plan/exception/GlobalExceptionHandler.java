package com.comprehensive.eureka.plan.exception;

import com.comprehensive.eureka.plan.dto.base.BaseResponseDto;
import com.comprehensive.eureka.plan.dto.response.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice("com.comprehensive.eureka.plan")
public class GlobalExceptionHandler {

    @ExceptionHandler(PlanException.class)
    public BaseResponseDto<ErrorResponseDto> handlePlanException(PlanException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(e.getErrorCode());
    }
}

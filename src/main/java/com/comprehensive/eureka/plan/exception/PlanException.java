package com.comprehensive.eureka.plan.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PlanException extends RuntimeException {

    private final ErrorCode errorCode;
}

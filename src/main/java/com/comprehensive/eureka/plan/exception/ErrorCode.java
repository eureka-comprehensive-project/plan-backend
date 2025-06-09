package com.comprehensive.eureka.plan.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNKNOWN_ERROR(40000, "UNKNOWN_ERROR", "알수없는 에러");

    private final int code;
    private final String name;
    private final String message;
}

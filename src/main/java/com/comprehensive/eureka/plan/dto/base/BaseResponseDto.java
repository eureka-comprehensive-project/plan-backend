package com.comprehensive.eureka.plan.dto.base;

import com.comprehensive.eureka.plan.dto.response.ErrorResponseDto;
import com.comprehensive.eureka.plan.exception.ErrorCode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BaseResponseDto<T> {
    private Integer statusCode;
    private String message;
    private T data;

    public static <T> BaseResponseDto<T> success(T data) {
        return BaseResponseDto.<T>builder()
                .statusCode(200)
                .message("success")
                .data(data)
                .build();
    }

    public static BaseResponseDto<Void> voidSuccess() {
        return BaseResponseDto.<Void>builder()
                .statusCode(200)
                .message("success")
                .data(null)
                .build();
    }

    public static BaseResponseDto<ErrorResponseDto> fail(ErrorCode errorCode) {
        return BaseResponseDto.<ErrorResponseDto>builder()
                .statusCode(errorCode.getCode())
                .message("fail")
                .data(ErrorResponseDto.of(errorCode))
                .build();
    }
}

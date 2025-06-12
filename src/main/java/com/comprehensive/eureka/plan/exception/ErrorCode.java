package com.comprehensive.eureka.plan.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    PLAN_NOT_FOUND(40000, "PLAN_NOT_FOUND", "요금제를 찾을 수 없습니다."),
    PLAN_ALREADY_EXISTS(40001, "PLAN_ALREADY_EXISTS", "이미 존재하는 요금제입니다."),
    PLAN_CREATE_FAILURE(40002, "PLAN_CREATE_FAILURE", "요금제 생성에 실패했습니다."),
    PLAN_UPDATE_FAILURE(40003, "PLAN_UPDATE_FAILURE", "요금제 수정에 실패했습니다."),
    PLAN_DELETE_FAILURE(40004, "PLAN_DELETE_FAILURE", "요금제 삭제에 실패했습니다."),
    PLAN_IN_USE(40005, "PLAN_IN_USE", "사용 중인 요금제는 삭제할 수 없습니다."),

    BENEFIT_NOT_FOUND(40030, "BENEFIT_NOT_FOUND", "존재하지 않는 혜택 ID가 포함되어 있습니다."),
    BENEFIT_GROUP_CREATE_FAILURE(40031, "BENEFIT_GROUP_CREATE_FAILURE", "혜택 그룹 생성에 실패했습니다."),
    BENEFIT_GROUP_NOT_FOUND(40032, "BENEFIT_GROUP_NOT_FOUND", "혜택 그룹을 찾을 수 없습니다."),
    INVALID_BENEFIT_COMBINATION(40033, "INVALID_BENEFIT_COMBINATION", "유효하지 않은 혜택 조합입니다."),
    PLAN_CATEGORY_EXCEPTION(40034, "PLAN_CATEGORY_EXCEPTION", "요금제 카테고리 예외가 발생했습니다."),
    DATA_ALLOWANCE_EXCEPTION(40035, "DATA_ALLOWANCE_EXCEPTION", "데이터 제공량 예외가 발생했습니다."),
    VOICE_CALL_EXCEPTION(40036, "VOICE_CALL_EXCEPTION", "음성 통화 예외가 발생했습니다."),
    SHARED_DATA_EXCEPTION(40037, "SHARED_DATA_EXCEPTION", "공유 데이터 예외가 발생했습니다.");

    private final int code;
    private final String name;
    private final String message;
}

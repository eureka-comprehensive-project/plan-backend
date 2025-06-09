package com.comprehensive.eureka.plan.enums;

import lombok.Getter;

@Getter
public enum BenefitType {
    PREMIUM("프리미엄"),
    MEDIA("미디어"),
    BASIC("기본");

    private final String label;

    BenefitType(String label) {
        this.label = label;
    }

    public BenefitType[] getAllBenefitType() {
        return BenefitType.values();
    }
}

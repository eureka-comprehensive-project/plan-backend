package com.comprehensive.eureka.plan.dto.request;

import lombok.Data;

@Data
public class GetPlanBenefitGroupIdRequestDto {
    private Integer planId;
    private Long benefitGroupId;
}

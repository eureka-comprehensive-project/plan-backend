package com.comprehensive.eureka.plan.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetPlanBenefitGroupIdRequestDto {
    private Integer planId;
    private Long benefitGroupId;
}

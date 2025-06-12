package com.comprehensive.eureka.plan.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlanBenefitDto {
    private Long planBenefitId;
    private Integer planId;
}

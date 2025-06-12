package com.comprehensive.eureka.plan.dto;

import com.comprehensive.eureka.plan.entity.enums.BenefitType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BenefitDto {

    private Long benefitId;
    private String benefitName;
    private BenefitType benefitType;
}

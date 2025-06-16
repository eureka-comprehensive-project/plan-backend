package com.comprehensive.eureka.plan.dto;

import com.comprehensive.eureka.plan.entity.enums.BenefitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BenefitDto {

    private Long benefitId;
    private String benefitName;
    private BenefitType benefitType;
}

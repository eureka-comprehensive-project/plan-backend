package com.comprehensive.eureka.plan.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class GetBenefitGroupsRequestDto {
    private List<Long> benefitIds;
}

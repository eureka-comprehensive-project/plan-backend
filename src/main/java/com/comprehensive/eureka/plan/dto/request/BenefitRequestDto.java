package com.comprehensive.eureka.plan.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BenefitRequestDto {
    private String premium;
    private String media;
}

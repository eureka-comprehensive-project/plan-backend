package com.comprehensive.eureka.plan.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlanDto {

    private String planName;
    private String planCategory;
    private Integer dataAllowance;
    private String dataAllowanceUnit;
    private Integer tetheringDataAmount;
    private String tetheringDataUnit;
    private Integer familyDataAmount;
    private String familyDataUnit;
    private Integer VoiceAllowance;
    private Integer additionalCallAllowance;
    private Integer monthlyFee;
    private List<Integer> benefitIdList;
}

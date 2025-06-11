package com.comprehensive.eureka.plan.dto;

import com.comprehensive.eureka.plan.entity.enums.DataPeriod;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlanDto {

    private Integer planId;
    private String planName;
    private String planCategory;
    private Integer dataAllowance;
    private String dataAllowanceUnit;
    private DataPeriod dataPeriod;
    private Integer tetheringDataAmount;
    private String tetheringDataUnit;
    private Integer familyDataAmount;
    private String familyDataUnit;
    private Integer VoiceAllowance;
    private Integer additionalCallAllowance;
    private Integer monthlyFee;
    private List<Long> benefitIdList;
}

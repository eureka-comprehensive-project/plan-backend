package com.comprehensive.eureka.plan.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanFilterResponseDto {

    private Integer planId;
    private String planName;
    private Integer monthlyFee;
    private String categoryName;
    private DataAllowanceInfo dataAllowance;
    private VoiceCallInfo voiceCall;
    private SharedDataInfo sharedData;
    private List<BenefitInfo> benefits;

    @Getter
    @Setter
    @Builder
    public static class DataAllowanceInfo {
        private Integer dataAmount;
        private String dataUnit;
        private String dataPeriod;
    }

    @Getter
    @Setter
    @Builder
    public static class VoiceCallInfo {
        private Integer voiceAllowance;
        private Integer additionalCallAllowance;
    }

    @Getter
    @Setter
    @Builder
    public static class SharedDataInfo {
        private Integer tetheringDataAmount;
        private String tetheringDataUnit;
        private Boolean familyDataAvailable;
        private Integer familyDataAmount;
        private String familyDataUnit;
    }

    @Getter
    @Setter
    @Builder
    public static class BenefitInfo {
        private Long benefitId;
        private String benefitName;
        private String benefitType;
    }
}

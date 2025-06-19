package com.comprehensive.eureka.plan.dto.response;

import com.comprehensive.eureka.plan.entity.Plan;
import com.comprehensive.eureka.plan.entity.PlanBenefitGroup;
import com.comprehensive.eureka.plan.entity.enums.DataPeriod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FilterListResponseDto {

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
    private Integer voiceAllowance;
    private Integer additionalCallAllowance;
    private Integer monthlyFee;
    private List<Long> benefitIdList;

    public static FilterListResponseDto fromEntity(Plan plan) {
        FilterListResponseDto dto = new FilterListResponseDto();
        dto.setPlanId(plan.getPlanId());
        dto.setPlanName(plan.getPlanName());

        if (plan.getPlanCategory() != null) {
            dto.setPlanCategory(plan.getPlanCategory().getCategoryName()); // Assuming PlanCategory has a getName() method
        }

        if (plan.getDataAllowances() != null) {
            dto.setDataAllowance(plan.getDataAllowances().getDataAmount());
            dto.setDataAllowanceUnit(plan.getDataAllowances().getDataUnit());
            dto.setDataPeriod(plan.getDataAllowances().getDataPeriod());
            dto.setTetheringDataAmount(plan.getSharedData().getTetheringDataAmount());
            dto.setTetheringDataUnit(plan.getSharedData().getTetheringDataUnit());
        }

        if (plan.getVoiceCall() != null) {
            dto.setVoiceAllowance(plan.getVoiceCall().getVoiceAllowance());
            dto.setAdditionalCallAllowance(plan.getVoiceCall().getAdditionalCallAllowance());
        }

        if (plan.getSharedData() != null) {
            dto.setFamilyDataAmount(plan.getSharedData().getFamilyDataAmount());
            dto.setFamilyDataUnit(plan.getSharedData().getFamilyDataUnit());
        }

        dto.setMonthlyFee(plan.getMonthlyFee());

        if (plan.getPlanBenefitGroups() != null) {
            dto.setBenefitIdList(plan.getPlanBenefitGroups().stream()
                    .map(PlanBenefitGroup::getPlanBenefitId) // Assuming PlanBenefitGroup has a getBenefitId() method that returns a Long
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
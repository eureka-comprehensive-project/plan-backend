package com.comprehensive.eureka.plan.service;

import com.comprehensive.eureka.plan.dto.request.PlanFilterRequest;
import com.comprehensive.eureka.plan.dto.response.PlanResponseDto;
import com.comprehensive.eureka.plan.entity.Plan;
import com.comprehensive.eureka.plan.repository.PlanRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanFilterService {

    private final PlanRepository planRepository;

    public List<PlanResponseDto> getFilteredPlans(PlanFilterRequest filterRequest) {
        List<Plan> plans = planRepository.findPlansWithFilter(filterRequest);

        return plans.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private PlanResponseDto convertToResponse(Plan plan) {
        return PlanResponseDto.builder()
                .planId(plan.getPlanId())
                .planName(plan.getPlanName())
                .monthlyFee(plan.getMonthlyFee())
                .categoryName(plan.getPlanCategory() != null ? plan.getPlanCategory().getCategoryName() : null)
                .dataAllowance(convertDataAllowance(plan))
                .voiceCall(convertVoiceCall(plan))
                .sharedData(convertSharedData(plan))
                .benefits(convertBenefits(plan))
                .build();
    }

    private PlanResponseDto.DataAllowanceInfo convertDataAllowance(Plan plan) {
        if (plan.getDataAllowances() == null) {
            return null;
        }

        return PlanResponseDto.DataAllowanceInfo.builder()
                .dataAmount(plan.getDataAllowances().getDataAmount())
                .dataUnit(plan.getDataAllowances().getDataUnit())
                .dataPeriod(plan.getDataAllowances().getDataPeriod().name())
                .build();
    }

    private PlanResponseDto.VoiceCallInfo convertVoiceCall(Plan plan) {
        if (plan.getVoiceCall() == null) {
            return null;
        }

        return PlanResponseDto.VoiceCallInfo.builder()
                .voiceAllowance(plan.getVoiceCall().getVoiceAllowance())
                .additionalCallAllowance(plan.getVoiceCall().getAdditionalCallAllowance())
                .build();
    }

    private PlanResponseDto.SharedDataInfo convertSharedData(Plan plan) {
        if (plan.getSharedData() == null) {
            return null;
        }

        return PlanResponseDto.SharedDataInfo.builder()
                .tetheringDataAmount(plan.getSharedData().getTetheringDataAmount())
                .tetheringDataUnit(plan.getSharedData().getTetheringDataUnit())
                .familyDataAvailable(plan.getSharedData().getFamilyDataAvailable())
                .familyDataAmount(plan.getSharedData().getFamilyDataAmount())
                .familyDataUnit(plan.getSharedData().getFamilyDataUnit())
                .build();
    }

    private List<PlanResponseDto.BenefitInfo> convertBenefits(Plan plan) {
        if (plan.getPlanBenefitGroups() == null) {
            return List.of();
        }

        return plan.getPlanBenefitGroups().stream()
                .filter(pbg -> pbg.getBenefitGroup() != null && pbg.getBenefitGroup().getBenefitGroupBenefits() != null)
                .flatMap(pbg -> pbg.getBenefitGroup().getBenefitGroupBenefits().stream())
                .filter(bgb -> bgb.getBenefit() != null)
                .map(bgb -> PlanResponseDto.BenefitInfo.builder()
                        .benefitId(bgb.getBenefit().getBenefitId())
                        .benefitName(bgb.getBenefit().getBenefitName())
                        .benefitType(bgb.getBenefit().getBenefitType().name())
                        .build())
                .distinct()
                .collect(Collectors.toList());
    }
}

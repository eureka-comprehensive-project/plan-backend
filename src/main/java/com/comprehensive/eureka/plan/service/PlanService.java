package com.comprehensive.eureka.plan.service;

import com.comprehensive.eureka.plan.dto.BenefitDto;
import com.comprehensive.eureka.plan.dto.PlanBenefitDto;
import com.comprehensive.eureka.plan.dto.PlanDto;
import com.comprehensive.eureka.plan.dto.request.PlanFilterRequestDto;
import com.comprehensive.eureka.plan.dto.response.FilterListResponseDto;
import com.comprehensive.eureka.plan.dto.response.PlanFilterResponseDto;

import java.util.List;

public interface PlanService {

    PlanDto createPlan(PlanDto planDto);
    List<PlanDto> getAllPlans();
    PlanDto updatePlan(Integer planId, PlanDto planDto);
    PlanDto getPlanById(Integer planId);
    List<BenefitDto> getAllBenefitsByPlanId(Integer planId);
    List<PlanBenefitDto> getPlansByPlanBenefitIds(List<Long> planBenefitIds);
    List<PlanFilterResponseDto> getFilteredPlans(PlanFilterRequestDto filterRequest);
    int countPlansWithFilter(PlanFilterRequestDto requestDto);
    boolean checkPlanHasBenefitGroup(Integer planId, Long benefitGroupId);
    List<PlanDto> findPlansByPlanNameContaining(String searchTerm);
    List<FilterListResponseDto> getFilteredList(PlanFilterRequestDto filterRequest);
}

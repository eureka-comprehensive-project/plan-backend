package com.comprehensive.eureka.plan.service;

import com.comprehensive.eureka.plan.dto.BenefitDto;
import com.comprehensive.eureka.plan.dto.PlanDto;

import java.util.List;

public interface PlanService {

    PlanDto createPlan(PlanDto planDto);
    List<PlanDto> getAllPlans();
    PlanDto updatePlan(Integer planId, PlanDto planDto);
    PlanDto getPlanById(Integer planId);
    List<BenefitDto> getAllBenefitsByPlanId(Integer planId);
}

package com.comprehensive.eureka.plan.service;

import com.comprehensive.eureka.plan.dto.PlanDto;
import com.comprehensive.eureka.plan.dto.request.PlanFilterRequest;
import com.comprehensive.eureka.plan.dto.response.PlanResponseDto;
import com.comprehensive.eureka.plan.entity.Plan;

import java.util.List;

public interface PlanService {

    PlanDto createPlan(PlanDto planDto);
    List<PlanDto> getAllPlans();
    PlanDto updatePlan(Integer planId, PlanDto planDto);
    PlanDto getPlanById(Integer planId);
}

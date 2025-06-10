package com.comprehensive.eureka.plan.service;

import com.comprehensive.eureka.plan.dto.PlanDto;

import java.util.List;

public interface PlanService {

    PlanDto createPlan(PlanDto planDto);
    List<PlanDto> getAllPlans();
    PlanDto getPlanById(Integer planId);
}

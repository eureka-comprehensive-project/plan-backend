package com.comprehensive.eureka.plan.repository;

import com.comprehensive.eureka.plan.dto.request.PlanFilterRequestDto;
import com.comprehensive.eureka.plan.entity.Plan;

import java.util.List;

public interface PlanRepositoryCustom {
    List<Plan> findPlansWithFilter(PlanFilterRequestDto filterRequest);
}

package com.comprehensive.eureka.plan.repository;

import com.comprehensive.eureka.plan.dto.request.PlanFilterRequest;
import com.comprehensive.eureka.plan.entity.Plan;

import java.util.List;

public interface PlanRepositoryCustom {
    List<Plan> findPlansWithFilter(PlanFilterRequest filterRequest);
}

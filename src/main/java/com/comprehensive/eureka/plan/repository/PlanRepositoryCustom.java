package com.comprehensive.eureka.plan.repository;

import com.comprehensive.eureka.plan.dto.request.PlanFilterRequestDto;
import com.comprehensive.eureka.plan.dto.response.FilterListResponseDto;
import com.comprehensive.eureka.plan.entity.BenefitGroup;
import com.comprehensive.eureka.plan.entity.Plan;

import java.util.List;
import java.util.Optional;

public interface PlanRepositoryCustom {
    List<Plan> findPlansWithFilter(PlanFilterRequestDto filterRequest);

    int countPlansWithFilter(PlanFilterRequestDto filterRequest);

    List<FilterListResponseDto> getFilteredList(PlanFilterRequestDto filterRequest);

    Optional<BenefitGroup> findBenefitGroupIdsByAllBenefitIds(List<Long> benefitIds);
}

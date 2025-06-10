package com.comprehensive.eureka.plan.repository;

import com.comprehensive.eureka.plan.entity.Plan;
import com.comprehensive.eureka.plan.entity.PlanBenefitGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanBenefitGroupRepository extends JpaRepository<PlanBenefitGroup, Long> {
    List<PlanBenefitGroup> findByPlan_PlanId(Integer planId);

    List<PlanBenefitGroup> findAllByPlan(Plan plan);
}

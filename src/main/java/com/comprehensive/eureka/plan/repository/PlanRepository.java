package com.comprehensive.eureka.plan.repository;

import com.comprehensive.eureka.plan.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Integer> {
    boolean existsByPlanName(String planName);
}

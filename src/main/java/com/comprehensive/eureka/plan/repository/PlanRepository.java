package com.comprehensive.eureka.plan.repository;

import com.comprehensive.eureka.plan.entity.Plan;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Integer> {
    boolean existsByPlanName(String planName);

    @Query("SELECT DISTINCT p FROM Plan p " +
            "LEFT JOIN FETCH p.dataAllowances " +
            "LEFT JOIN FETCH p.voiceCall " +
            "LEFT JOIN FETCH p.sharedData " +
            "LEFT JOIN FETCH p.planCategory " +
            "LEFT JOIN FETCH p.planBenefitGroups")
    List<Plan> findAllWithBasicDetails();

    Optional<Plan> findByPlanName(String planName);
}

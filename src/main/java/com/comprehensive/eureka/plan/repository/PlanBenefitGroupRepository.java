package com.comprehensive.eureka.plan.repository;

import com.comprehensive.eureka.plan.entity.Plan;
import com.comprehensive.eureka.plan.entity.PlanBenefitGroup;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlanBenefitGroupRepository extends JpaRepository<PlanBenefitGroup, Long> {
    List<PlanBenefitGroup> findByPlan_PlanId(Integer planId);

    List<PlanBenefitGroup> findAllByPlan(Plan plan);

    @Query("SELECT DISTINCT pbg FROM PlanBenefitGroup pbg " +
            "JOIN FETCH pbg.benefitGroup bg " +
            "JOIN FETCH bg.benefitGroupBenefits bgb " +
            "JOIN FETCH bgb.benefit b " +
            "WHERE pbg.plan.planId = :planId")
    List<PlanBenefitGroup> findAllWithBenefitsByPlanId(@Param("planId") Integer planId);

    @Query("SELECT pbg FROM PlanBenefitGroup pbg " +
            "JOIN FETCH pbg.plan " +
            "WHERE pbg.planBenefitId IN :planBenefitIds")
    List<PlanBenefitGroup> findAllByIdWithPlan(@Param("planBenefitIds") List<Long> planBenefitIds);

    boolean existsByPlan_PlanIdAndBenefitGroup_BenefitGroupId(Integer planId, Long benefitGroupId);

    @Query("SELECT pbg FROM PlanBenefitGroup pbg " +
            "LEFT JOIN FETCH pbg.benefitGroup bg " +
            "LEFT JOIN FETCH bg.benefitGroupBenefits bgb " +
            "LEFT JOIN FETCH bgb.benefit b " +
            "WHERE pbg.planBenefitId = :planBenefitId")
    Optional<PlanBenefitGroup> findPlanBenefitGroupWithBenefits(@Param("planBenefitId") Long planBenefitId);
}

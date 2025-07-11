package com.comprehensive.eureka.plan.repository;

import com.comprehensive.eureka.plan.dto.request.GetPlanBenefitGroupIdRequestDto;
import com.comprehensive.eureka.plan.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Integer>, PlanRepositoryCustom {
    boolean existsByPlanName(String planName);

    Optional<Plan> findByPlanName(String planName);

    @Query("SELECT DISTINCT p FROM Plan p " +
            "LEFT JOIN FETCH p.planCategory " +
            "LEFT JOIN FETCH p.dataAllowances " +
            "LEFT JOIN FETCH p.voiceCall " +
            "LEFT JOIN FETCH p.sharedData " +
            "LEFT JOIN FETCH p.planBenefitGroups pbg " +
            "LEFT JOIN FETCH pbg.benefitGroup bg " +
            "LEFT JOIN FETCH bg.benefitGroupBenefits bgb " +
            "LEFT JOIN FETCH bgb.benefit")
    List<Plan> findAllWithBenefits();

    @Query("SELECT p FROM Plan p " +
            "LEFT JOIN FETCH p.planCategory " +
            "LEFT JOIN FETCH p.dataAllowances " +
            "LEFT JOIN FETCH p.voiceCall " +
            "LEFT JOIN FETCH p.sharedData " +
            "LEFT JOIN FETCH p.planBenefitGroups pbg " +
            "LEFT JOIN FETCH pbg.benefitGroup bg " +
            "LEFT JOIN FETCH bg.benefitGroupBenefits bgb " +
            "LEFT JOIN FETCH bgb.benefit b " +
            "WHERE p.planId = :planId")
    Optional<Plan> findWithBenefitsById(@Param("planId") Integer planId);

    @Query("SELECT p FROM Plan p " +
            "LEFT JOIN FETCH p.planCategory pc " +
            "LEFT JOIN FETCH p.dataAllowances da " +
            "LEFT JOIN FETCH p.voiceCall vc " +
            "LEFT JOIN FETCH p.sharedData sd " +
            "LEFT JOIN FETCH p.planBenefitGroups pbg " +
            "LEFT JOIN FETCH pbg.benefitGroup bg " +
            "LEFT JOIN FETCH bg.benefitGroupBenefits bgb " +
            "LEFT JOIN FETCH bgb.benefit b " +
            "WHERE LOWER(p.planName) LIKE LOWER(CONCAT('%', :planName, '%'))")
    List<Plan> findByPlanNameContainingIgnoreCase(@Param("planName") String planName);

}

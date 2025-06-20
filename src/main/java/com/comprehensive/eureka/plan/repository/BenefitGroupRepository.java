package com.comprehensive.eureka.plan.repository;

import com.comprehensive.eureka.plan.entity.BenefitGroup;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BenefitGroupRepository extends JpaRepository<BenefitGroup, Long> {

    @Query("SELECT DISTINCT bg FROM BenefitGroup bg " +
            "JOIN FETCH bg.benefitGroupBenefits bgb " +
            "JOIN FETCH bgb.benefit b " +
            "WHERE b.benefitId IN :benefitIds")
    List<BenefitGroup> findByBenefitIdsWithBenefits(@Param("benefitIds") Set<Long> benefitIds);

    @Query("SELECT bg FROM BenefitGroup bg " +
            "WHERE NOT EXISTS (" +
            "  SELECT b.benefitId FROM Benefit b " +
            "  WHERE b.benefitId IN :benefitIdList AND b.benefitId NOT IN (" +
            "    SELECT bgb.benefit.benefitId FROM BenefitGroupBenefit bgb WHERE bgb.benefitGroup = bg" +
            "  )" +
            ") AND NOT EXISTS (" +
            "  SELECT bgb.benefit.benefitId FROM BenefitGroupBenefit bgb " +
            "  WHERE bgb.benefitGroup = bg AND bgb.benefit.benefitId NOT IN :benefitIdList" +
            ")")
    List<BenefitGroup> findBenefitGroupsByExactBenefits(@Param("benefitIdList") List<Long> benefitIdList);

    @Query("SELECT DISTINCT bg FROM BenefitGroup bg JOIN bg.benefitGroupBenefits bgb WHERE bgb.benefit.benefitId IN :benefitIds")
    List<BenefitGroup> findAllByBenefitGroupBenefitsBenefitBenefitIdIn(@Param("benefitIds") Set<Long> benefitIds);
}

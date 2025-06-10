package com.comprehensive.eureka.plan.repository;

import com.comprehensive.eureka.plan.entity.BenefitGroup;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BenefitGroupRepository extends JpaRepository<BenefitGroup, Long> {

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
}

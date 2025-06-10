package com.comprehensive.eureka.plan.repository;

import com.comprehensive.eureka.plan.entity.BenefitGroupBenefit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BenefitGroupBenefitRepository extends JpaRepository<BenefitGroupBenefit, Long> {
    List<BenefitGroupBenefit> findByBenefitGroup_BenefitGroupId(Long benefitGroupId);
}

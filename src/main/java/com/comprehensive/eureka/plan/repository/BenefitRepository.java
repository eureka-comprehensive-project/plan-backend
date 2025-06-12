package com.comprehensive.eureka.plan.repository;

import com.comprehensive.eureka.plan.entity.Benefit;
import com.comprehensive.eureka.plan.entity.enums.BenefitType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BenefitRepository extends JpaRepository<Benefit, Long> {

    List<Benefit> findAllByBenefitType(BenefitType benefitType);
}


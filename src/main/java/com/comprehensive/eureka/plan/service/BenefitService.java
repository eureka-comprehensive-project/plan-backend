package com.comprehensive.eureka.plan.service;

import com.comprehensive.eureka.plan.dto.BenefitDto;
import java.util.List;

public interface BenefitService {

    List<BenefitDto> getAllBenefitsByType(String benefitType);
    List<BenefitDto> getAllBenefits();
}

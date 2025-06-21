package com.comprehensive.eureka.plan.service;

import com.comprehensive.eureka.plan.dto.BenefitDto;
import com.comprehensive.eureka.plan.dto.request.BenefitRequestDto;
import java.util.List;
import java.util.Set;

public interface BenefitService {

    List<BenefitDto> getAllBenefitsByType(String benefitType);
    List<BenefitDto> getAllBenefits();
    Long findBenefitGroupIdByBenefits(BenefitRequestDto benefitRequestDto);
    Set<BenefitDto> getBenefitsByPlanBenefitGroupId(Long planBenefitGroupId);
}

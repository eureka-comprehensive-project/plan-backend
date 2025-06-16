package com.comprehensive.eureka.plan.controller;

import com.comprehensive.eureka.plan.dto.BenefitDto;
import com.comprehensive.eureka.plan.dto.base.BaseResponseDto;
import com.comprehensive.eureka.plan.service.BenefitService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/plan/benefit")
public class BenefitController {

    private final BenefitService benefitService;

    @GetMapping("/{benefitType}")
    public BaseResponseDto<List<BenefitDto>> getAllBenefitsByType(@PathVariable String benefitType) {
        List<BenefitDto> benefits = benefitService.getAllBenefitsByType(benefitType);
        return BaseResponseDto.success(benefits);
    }

    @GetMapping("/")
    public BaseResponseDto<List<BenefitDto>> getAllBenefits() {
        List<BenefitDto> benefits = benefitService.getAllBenefits();
        return BaseResponseDto.success(benefits);
    }
}

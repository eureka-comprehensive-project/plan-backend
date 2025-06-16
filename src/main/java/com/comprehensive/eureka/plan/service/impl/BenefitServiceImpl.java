package com.comprehensive.eureka.plan.service.impl;

import com.comprehensive.eureka.plan.dto.BenefitDto;
import com.comprehensive.eureka.plan.entity.Benefit;
import com.comprehensive.eureka.plan.entity.enums.BenefitType;
import com.comprehensive.eureka.plan.exception.ErrorCode;
import com.comprehensive.eureka.plan.exception.PlanException;
import com.comprehensive.eureka.plan.repository.BenefitRepository;
import com.comprehensive.eureka.plan.service.BenefitService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BenefitServiceImpl implements BenefitService {

    private final BenefitRepository benefitRepository;

    @Override
    public List<BenefitDto> getAllBenefitsByType(String benefitType) {
        try {
            BenefitType type = BenefitType.valueOf(benefitType.toUpperCase());
            List<Benefit> benefits = benefitRepository.findAllByBenefitType(type);

            return benefits.stream()
                    .map(benefit -> BenefitDto.builder()
                            .benefitId(benefit.getBenefitId())
                            .benefitName(benefit.getBenefitName())
                            .benefitType(benefit.getBenefitType())
                            .build())
                    .toList();

        } catch (IllegalArgumentException e) {
            log.warn("유효하지 않은 혜택 타입으로 조회 시도: {}", benefitType);
            throw new PlanException(ErrorCode.BENEFIT_NOT_FOUND);
        }
    }

    @Override
    public List<BenefitDto> getAllBenefits() {
        List<Benefit> benefits = benefitRepository.findAll();

        return benefits.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private BenefitDto convertToDto(Benefit benefit) {
        BenefitDto dto = new BenefitDto();
        dto.setBenefitId(benefit.getBenefitId());
        dto.setBenefitName(benefit.getBenefitName());
        dto.setBenefitType(benefit.getBenefitType());
        return dto;
    }
}

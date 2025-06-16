package com.comprehensive.eureka.plan.service.impl;

import com.comprehensive.eureka.plan.dto.BenefitDto;
import com.comprehensive.eureka.plan.dto.request.BenefitRequestDto;
import com.comprehensive.eureka.plan.entity.Benefit;
import com.comprehensive.eureka.plan.entity.BenefitGroup;
import com.comprehensive.eureka.plan.entity.enums.BenefitType;
import com.comprehensive.eureka.plan.exception.ErrorCode;
import com.comprehensive.eureka.plan.exception.PlanException;
import com.comprehensive.eureka.plan.repository.BenefitGroupRepository;
import com.comprehensive.eureka.plan.repository.BenefitRepository;
import com.comprehensive.eureka.plan.service.BenefitService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class BenefitServiceImpl implements BenefitService {

    private final BenefitRepository benefitRepository;
    private final BenefitGroupRepository benefitGroupRepository;

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

    @Override
    public Long findBenefitGroupIdByBenefits(BenefitRequestDto benefitRequestDto) {
        log.info("findBenefitGroupIdByBenefits: {}", benefitRequestDto);
        List<String> benefitNames = Stream.of(benefitRequestDto.getPremium(), benefitRequestDto.getMedia())
                .filter(StringUtils::hasText)
                .toList();

        if (benefitNames.isEmpty()) return 0L;

        List<Benefit> foundBenefits = benefitRepository.findAllByBenefitNameIn(benefitNames);

        if (foundBenefits.size() != benefitNames.size()) {
            log.warn("일부 혜택이 존재하지 않습니다: {}", benefitNames);
            return 0L;
        }

        List<Long> benefitIds = foundBenefits.stream()
                .map(Benefit::getBenefitId)
                .toList();

        List<BenefitGroup> benefitGroups = benefitGroupRepository.findBenefitGroupsByExactBenefits(benefitIds);

        if (benefitGroups.isEmpty()) return 0L;

        if (benefitGroups.size() > 1) {
            log.warn("여러 개의 동일한 혜택 그룹이 발견되었습니다: {}", benefitGroups);
            throw new PlanException(ErrorCode.BENEFIT_GROUP_NOT_FOUND);
        }

        BenefitGroup benefitGroup = benefitGroups.get(0);

        return benefitGroup.getBenefitGroupId();
    }

    private BenefitDto convertToDto(Benefit benefit) {
        BenefitDto dto = new BenefitDto();
        dto.setBenefitId(benefit.getBenefitId());
        dto.setBenefitName(benefit.getBenefitName());
        dto.setBenefitType(benefit.getBenefitType());
        return dto;
    }
}

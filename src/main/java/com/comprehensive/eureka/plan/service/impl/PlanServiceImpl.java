package com.comprehensive.eureka.plan.service.impl;

import com.comprehensive.eureka.plan.dto.PlanDto;
import com.comprehensive.eureka.plan.entity.Benefit;
import com.comprehensive.eureka.plan.entity.BenefitGroup;
import com.comprehensive.eureka.plan.entity.BenefitGroupBenefit;
import com.comprehensive.eureka.plan.entity.DataAllowances;
import com.comprehensive.eureka.plan.entity.Plan;
import com.comprehensive.eureka.plan.entity.PlanBenefitGroup;
import com.comprehensive.eureka.plan.entity.PlanCategory;
import com.comprehensive.eureka.plan.entity.SharedData;
import com.comprehensive.eureka.plan.entity.VoiceCall;
import com.comprehensive.eureka.plan.entity.enums.BenefitType;
import com.comprehensive.eureka.plan.entity.enums.DataPeriod;
import com.comprehensive.eureka.plan.exception.ErrorCode;
import com.comprehensive.eureka.plan.exception.PlanException;
import com.comprehensive.eureka.plan.repository.BenefitGroupBenefitRepository;
import com.comprehensive.eureka.plan.repository.BenefitGroupRepository;
import com.comprehensive.eureka.plan.repository.BenefitRepository;
import com.comprehensive.eureka.plan.repository.DataAllowanceRepository;
import com.comprehensive.eureka.plan.repository.PlanBenefitGroupRepository;
import com.comprehensive.eureka.plan.repository.PlanCategoryRepository;
import com.comprehensive.eureka.plan.repository.PlanRepository;
import com.comprehensive.eureka.plan.repository.SharedDataRepository;
import com.comprehensive.eureka.plan.repository.VoiceCallRepository;
import com.comprehensive.eureka.plan.service.PlanService;
import com.comprehensive.eureka.plan.service.util.DuplicateChecker;
import jakarta.transaction.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final DuplicateChecker duplicateChecker;

    private final PlanRepository planRepository;
    private final BenefitRepository benefitRepository;
    private final PlanBenefitGroupRepository planBenefitGroupRepository;
    private final BenefitGroupBenefitRepository benefitGroupBenefitRepository;

    @Override
    @Transactional
    public PlanDto createPlan(PlanDto planDto) {
        log.info("요금제 생성 요청 시작: {}", planDto.getPlanName());

        if (planRepository.existsByPlanName(planDto.getPlanName())) {
            log.warn("이미 존재하는 요금제 이름으로 생성 시도: {}", planDto.getPlanName());
            throw new PlanException(ErrorCode.PLAN_ALREADY_EXISTS);
        }

        try {
            PlanCategory category = duplicateChecker.findOrCreatePlanCategory(planDto.getPlanCategory());
            DataAllowances dataAllowances = duplicateChecker.findOrCreateDataAllowances(planDto);
            VoiceCall voiceCall = duplicateChecker.findOrCreateVoiceCall(planDto);
            SharedData sharedData = duplicateChecker.findOrCreateSharedData(planDto);

            Plan newPlan = new Plan();
            newPlan.setPlanName(planDto.getPlanName());
            newPlan.setMonthlyFee(planDto.getMonthlyFee());
            newPlan.setPlanCategory(category);
            newPlan.setDataAllowances(dataAllowances);
            newPlan.setVoiceCall(voiceCall);
            newPlan.setSharedData(sharedData);

            Plan savedPlan = planRepository.save(newPlan);

            List<BenefitGroup> benefitGroups = processBenefitCombinations(planDto.getBenefitIdList());

            if (!benefitGroups.isEmpty()) {
                List<PlanBenefitGroup> planBenefitGroups = benefitGroups.stream()
                        .map(group -> {
                            PlanBenefitGroup pbg = new PlanBenefitGroup();
                            pbg.setPlan(savedPlan);
                            pbg.setBenefitGroup(group);
                            return pbg;
                        })
                        .collect(Collectors.toList());
                planBenefitGroupRepository.saveAll(planBenefitGroups);
            }

            log.info("요금제 생성 성공: {}", savedPlan.getPlanName());

            return convertToDto(savedPlan, planDto.getBenefitIdList());

        } catch (PlanException e) {
            throw e;

        } catch (Exception e) {
            log.error("요금제 생성 중 예측하지 못한 오류 발생. 요금제 이름: {}", planDto.getPlanName(), e);
            throw new PlanException(ErrorCode.PLAN_CREATE_FAILURE);
        }
    }

    private List<BenefitGroup> processBenefitCombinations(List<Long> benefitIdList) {
        if (benefitIdList == null || benefitIdList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Benefit> allBenefits = benefitRepository.findAllById(benefitIdList);

        if (allBenefits.size() != benefitIdList.size()) {
            log.warn("요청된 혜택 ID 중 일부를 찾을 수 없음. 요청 ID: {}", benefitIdList);
            throw new PlanException(ErrorCode.BENEFIT_NOT_FOUND);
        }
        List<BenefitGroup> singleBenefitGroups = allBenefits.stream()
                .map(benefit -> duplicateChecker.findOrCreateBenefitGroupForCombination(List.of(benefit)))
                .toList();

        Map<BenefitType, List<Benefit>> benefitsByType = allBenefits.stream()
                .collect(Collectors.groupingBy(Benefit::getBenefitType));

        List<BenefitType> types = new ArrayList<>(benefitsByType.keySet());
        List<BenefitGroup> pairBenefitGroups = new ArrayList<>();

        for (int i = 0; i < types.size(); i++) {
            for (int j = i + 1; j < types.size(); j++) {
                List<Benefit> list1 = benefitsByType.get(types.get(i));
                List<Benefit> list2 = benefitsByType.get(types.get(j));

                for (Benefit benefit1 : list1) {
                    for (Benefit benefit2 : list2) {
                        pairBenefitGroups.add(duplicateChecker.findOrCreateBenefitGroupForCombination(List.of(benefit1, benefit2)));
                    }
                }
            }
        }
        return Stream.concat(singleBenefitGroups.stream(), pairBenefitGroups.stream())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<PlanDto> getAllPlans() {
        List<Plan> plans = planRepository.findAllWithBasicDetails();

        return plans.stream().map(plan -> {
            Set<Long> benefitIds = new HashSet<>();

            List<PlanBenefitGroup> planBenefitGroups = planBenefitGroupRepository.findByPlan_PlanId(plan.getPlanId());

            for (PlanBenefitGroup planBenefitGroup : planBenefitGroups) {
                List<BenefitGroupBenefit> groupBenefits =
                        benefitGroupBenefitRepository.findByBenefitGroup_BenefitGroupId(
                                planBenefitGroup.getBenefitGroup().getBenefitGroupId());

                for (BenefitGroupBenefit groupBenefit : groupBenefits) {
                    benefitIds.add(groupBenefit.getBenefit().getBenefitId());
                }
            }

            return PlanDto.builder()
                    .planName(plan.getPlanName())
                    .planCategory(plan.getPlanCategory() != null ? plan.getPlanCategory().getCategoryName() : null)
                    .dataAllowance(plan.getDataAllowances() != null ? plan.getDataAllowances().getDataAmount() : null)
                    .dataAllowanceUnit(plan.getDataAllowances() != null ? plan.getDataAllowances().getDataUnit() : null)
                    .dataPeriod(plan.getDataAllowances() != null ? plan.getDataAllowances().getDataPeriod() : null)
                    .tetheringDataAmount(plan.getSharedData() != null ? plan.getSharedData().getTetheringDataAmount() : null)
                    .tetheringDataUnit(plan.getSharedData() != null ? plan.getSharedData().getTetheringDataUnit() : null)
                    .familyDataAmount(plan.getSharedData() != null ? plan.getSharedData().getFamilyDataAmount() : null)
                    .familyDataUnit(plan.getSharedData() != null ? plan.getSharedData().getFamilyDataUnit() : null)
                    .VoiceAllowance(plan.getVoiceCall() != null ? plan.getVoiceCall().getVoiceAllowance() : null)
                    .additionalCallAllowance(plan.getVoiceCall() != null ? plan.getVoiceCall().getAdditionalCallAllowance() : null)
                    .monthlyFee(plan.getMonthlyFee())
                    .benefitIdList(new ArrayList<>(benefitIds))
                    .build();
        }).collect(Collectors.toList());
    }

    private PlanDto convertToDto(Plan plan, List<Long> originalBenefitIds) {
        return PlanDto.builder()
                .planName(plan.getPlanName())
                .planCategory(plan.getPlanCategory().getCategoryName())
                .dataAllowance(plan.getDataAllowances().getDataAmount())
                .dataAllowanceUnit(plan.getDataAllowances().getDataUnit())
                .dataPeriod(plan.getDataAllowances().getDataPeriod())
                .tetheringDataAmount(plan.getSharedData().getTetheringDataAmount())
                .tetheringDataUnit(plan.getSharedData().getTetheringDataUnit())
                .familyDataAmount(plan.getSharedData().getFamilyDataAmount())
                .familyDataUnit(plan.getSharedData().getFamilyDataUnit())
                .VoiceAllowance(plan.getVoiceCall().getVoiceAllowance())
                .additionalCallAllowance(plan.getVoiceCall().getAdditionalCallAllowance())
                .monthlyFee(plan.getMonthlyFee())
                .benefitIdList(originalBenefitIds)
                .build();
    }
}

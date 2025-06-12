package com.comprehensive.eureka.plan.service.impl;

import com.comprehensive.eureka.plan.dto.PlanDto;
import com.comprehensive.eureka.plan.dto.request.PlanFilterRequest;
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
import com.comprehensive.eureka.plan.repository.*;
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
                    .planId(plan.getPlanId())
                    .planName(plan.getPlanName())
                    .planCategory(plan.getPlanCategory() != null ? plan.getPlanCategory().getCategoryName() : null)
                    .dataAllowance(plan.getDataAllowances() != null ? plan.getDataAllowances().getDataAmount() : null)
                    .dataAllowanceUnit(plan.getDataAllowances() != null ? plan.getDataAllowances().getDataUnit() : null)
                    .dataPeriod(plan.getDataAllowances() != null ? plan.getDataAllowances().getDataPeriod() : null)
                    .tetheringDataAmount(plan.getSharedData() != null ? plan.getSharedData().getTetheringDataAmount() : null)
                    .tetheringDataUnit(plan.getSharedData() != null ? plan.getSharedData().getTetheringDataUnit() : null)
                    .familyDataAmount(plan.getSharedData() != null ? plan.getSharedData().getFamilyDataAmount() : null)
                    .familyDataUnit(plan.getSharedData() != null ? plan.getSharedData().getFamilyDataUnit() : null)
                    .voiceAllowance(plan.getVoiceCall() != null ? plan.getVoiceCall().getVoiceAllowance() : null)
                    .additionalCallAllowance(plan.getVoiceCall() != null ? plan.getVoiceCall().getAdditionalCallAllowance() : null)
                    .monthlyFee(plan.getMonthlyFee())
                    .benefitIdList(new ArrayList<>(benefitIds))
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PlanDto updatePlan(Integer planId, PlanDto planDto) {
        log.info("요금제 수정 요청 시작: ID {}", planId);

        Plan planToUpdate = planRepository.findById(planId)
                .orElseThrow(() -> new PlanException(ErrorCode.PLAN_NOT_FOUND));

        planRepository.findByPlanName(planDto.getPlanName()).ifPresent(plan -> {
            if (!plan.getPlanId().equals(planId)) {
                log.warn("이미 존재하는 요금제 이름으로 수정 시도: {}", planDto.getPlanName());
                throw new PlanException(ErrorCode.PLAN_ALREADY_EXISTS);
            }
        });

        try {
            PlanCategory category = duplicateChecker.findOrCreatePlanCategory(planDto.getPlanCategory());
            DataAllowances dataAllowances = duplicateChecker.findOrCreateDataAllowances(planDto);
            VoiceCall voiceCall = duplicateChecker.findOrCreateVoiceCall(planDto);
            SharedData sharedData = duplicateChecker.findOrCreateSharedData(planDto);

            planToUpdate.setPlanName(planDto.getPlanName());
            planToUpdate.setMonthlyFee(planDto.getMonthlyFee());
            planToUpdate.setPlanCategory(category);
            planToUpdate.setDataAllowances(dataAllowances);
            planToUpdate.setVoiceCall(voiceCall);
            planToUpdate.setSharedData(sharedData);

            updatePlanBenefits(planToUpdate, planDto.getBenefitIdList());

            log.info("요금제 수정 성공: {}", planToUpdate.getPlanName());
            return convertToDto(planToUpdate, planDto.getBenefitIdList());

        } catch (PlanException e) {
            throw e;
        } catch (Exception e) {
            log.error("요금제 수정 중 예측하지 못한 오류 발생. 요금제 ID: {}", planId, e);
            throw new PlanException(ErrorCode.PLAN_UPDATE_FAILURE);
        }
    }

    private void updatePlanBenefits(Plan plan, List<Long> newBenefitIdList) {
        Set<Long> currentBenefitIds = planBenefitGroupRepository.findAllByPlan(plan).stream()
                .flatMap(pbg -> pbg.getBenefitGroup().getBenefitGroupBenefits().stream())
                .map(bgb -> bgb.getBenefit().getBenefitId())
                .collect(Collectors.toSet());

        Set<Long> newBenefitIds = new HashSet<>(newBenefitIdList);

        Set<Long> removedBenefitIds = new HashSet<>(currentBenefitIds);
        removedBenefitIds.removeAll(newBenefitIds);

        if (!removedBenefitIds.isEmpty()) {
            List<PlanBenefitGroup> groupsToRemove = planBenefitGroupRepository.findAllByPlan(plan).stream()
                    .filter(pbg -> pbg.getBenefitGroup().getBenefitGroupBenefits().stream()
                            .anyMatch(bgb -> removedBenefitIds.contains(bgb.getBenefit().getBenefitId())))
                    .toList();

            planBenefitGroupRepository.deleteAll(groupsToRemove);
            log.info("요금제 ID {}: 제거된 혜택이 포함된 그룹 연결 {}개 해제", plan.getPlanId(), groupsToRemove.size());
        }

        if (!newBenefitIdList.isEmpty()) {
            List<BenefitGroup> newAllBenefitGroups = processBenefitCombinations(newBenefitIdList);

            Set<Long> currentGroupIds = planBenefitGroupRepository.findAllByPlan(plan).stream()
                    .map(pbg -> pbg.getBenefitGroup().getBenefitGroupId())
                    .collect(Collectors.toSet());

            List<PlanBenefitGroup> groupsToAdd = newAllBenefitGroups.stream()
                    .filter(group -> !currentGroupIds.contains(group.getBenefitGroupId()))
                    .map(group -> {
                        PlanBenefitGroup pbg = new PlanBenefitGroup();
                        pbg.setPlan(plan);
                        pbg.setBenefitGroup(group);
                        return pbg;
                    })
                    .toList();

            if (!groupsToAdd.isEmpty()) {
                planBenefitGroupRepository.saveAll(groupsToAdd);
                log.info("요금제 ID {}: 새로운 혜택 조합 그룹 {}개 연결", plan.getPlanId(), groupsToAdd.size());
            }
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
    public PlanDto getPlanById(Integer planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow( () -> new PlanException(ErrorCode.PLAN_NOT_FOUND));

        Set<Long> benefitIds = new HashSet<>();

        List<PlanBenefitGroup> planBenefitGroups = planBenefitGroupRepository.findByPlan_PlanId(planId);

        for (PlanBenefitGroup planBenefitGroup : planBenefitGroups) {
            List<BenefitGroupBenefit> groupBenefits =
                    benefitGroupBenefitRepository.findByBenefitGroup_BenefitGroupId(
                            planBenefitGroup.getBenefitGroup().getBenefitGroupId());

            for (BenefitGroupBenefit groupBenefit : groupBenefits) {
                benefitIds.add(groupBenefit.getBenefit().getBenefitId());
            }
        }

        return convertToDto(plan, new ArrayList<>(benefitIds));

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
                .voiceAllowance(plan.getVoiceCall().getVoiceAllowance())
                .additionalCallAllowance(plan.getVoiceCall().getAdditionalCallAllowance())
                .monthlyFee(plan.getMonthlyFee())
                .benefitIdList(originalBenefitIds)
                .build();
    }
}

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
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final PlanCategoryRepository planCategoryRepository;
    private final DataAllowanceRepository dataAllowanceRepository;
    private final VoiceCallRepository voiceCallRepository;
    private final SharedDataRepository sharedDataRepository;
    private final BenefitGroupRepository benefitGroupRepository;
    private final BenefitRepository benefitRepository;
    private final BenefitGroupBenefitRepository benefitGroupBenefitRepository;
    private final PlanBenefitGroupRepository planBenefitGroupRepository;

    @Override
    @Transactional
    public PlanDto createPlan(PlanDto planDto) {
        if (planRepository.existsByPlanName(planDto.getPlanName())) {
            throw new IllegalArgumentException("이미 존재하는 요금제 이름입니다: " + planDto.getPlanName());
        }

        PlanCategory category = findOrCreatePlanCategory(planDto.getPlanCategory());
        DataAllowances dataAllowances = findOrCreateDataAllowances(planDto);
        VoiceCall voiceCall = findOrCreateVoiceCall(planDto);
        SharedData sharedData = findOrCreateSharedData(planDto);

        Plan newPlan = new Plan();
        newPlan.setPlanName(planDto.getPlanName());
        newPlan.setMonthlyFee(planDto.getMonthlyFee());
        newPlan.setPlanCategory(category);
        newPlan.setDataAllowances(dataAllowances);
        newPlan.setVoiceCall(voiceCall);
        newPlan.setSharedData(sharedData);
        Plan savedPlan = planRepository.save(newPlan);

        List<BenefitGroup> benefitGroups = createBenefitCombinationsAndGetGroups(planDto.getBenefitIdList(), planDto.getPlanName());

        for (BenefitGroup benefitGroup : benefitGroups) {
            PlanBenefitGroup planBenefitGroup = new PlanBenefitGroup();
            planBenefitGroup.setPlan(savedPlan);
            planBenefitGroup.setBenefitGroup(benefitGroup);
            planBenefitGroupRepository.save(planBenefitGroup);
        }

        return convertToDto(savedPlan, planDto.getBenefitIdList());
    }

    private List<BenefitGroup> createBenefitCombinationsAndGetGroups(List<Integer> benefitIdList, String planName) {
        if (benefitIdList == null || benefitIdList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> longBenefitIdList = benefitIdList.stream().map(Long::valueOf).collect(Collectors.toList());
        List<Benefit> allBenefits = benefitRepository.findAllById(longBenefitIdList);

        if (allBenefits.size() != longBenefitIdList.size()) {
            throw new EntityNotFoundException("일부 혜택 ID를 찾을 수 없습니다.");
        }

        List<BenefitGroup> resultingGroups = new ArrayList<>();

        for (Benefit benefit : allBenefits) {
            List<Benefit> singleCombination = List.of(benefit);
            BenefitGroup group = findOrCreateBenefitGroupForCombination(singleCombination);
            resultingGroups.add(group);
        }

        Map<BenefitType, List<Benefit>> benefitsByType = allBenefits.stream()
                .collect(Collectors.groupingBy(Benefit::getBenefitType));

        List<BenefitType> types = new ArrayList<>(benefitsByType.keySet());

        for (int i = 0; i < types.size(); i++) {
            for (int j = i + 1; j < types.size(); j++) {
                List<Benefit> list1 = benefitsByType.get(types.get(i));
                List<Benefit> list2 = benefitsByType.get(types.get(j));

                for (Benefit benefit1 : list1) {
                    for (Benefit benefit2 : list2) {
                        List<Benefit> pairCombination = List.of(benefit1, benefit2);
                        BenefitGroup group = findOrCreateBenefitGroupForCombination(pairCombination);
                        resultingGroups.add(group);
                    }
                }
            }
        }
        return resultingGroups;
    }

    private BenefitGroup findOrCreateBenefitGroupForCombination(List<Benefit> combination) {
        List<Long> combinationIds = combination.stream()
                .map(Benefit::getBenefitId)
                .sorted()
                .collect(Collectors.toList());

        List<BenefitGroup> existingGroups = benefitGroupRepository.findBenefitGroupsByExactBenefits(combinationIds);
        if (!existingGroups.isEmpty()) {
            return existingGroups.get(0);
        }

        String description = combination.stream()
                .map(Benefit::getBenefitName)
                .collect(Collectors.joining(" & ")) + " 조합";

        BenefitGroup newBenefitGroup = new BenefitGroup();
        newBenefitGroup.setDescription(description);
        BenefitGroup savedBenefitGroup = benefitGroupRepository.save(newBenefitGroup);

        for (Benefit benefit : combination) {
            BenefitGroupBenefit join = new BenefitGroupBenefit();
            join.setBenefitGroup(savedBenefitGroup);
            join.setBenefit(benefit);
            benefitGroupBenefitRepository.save(join);
        }

        return savedBenefitGroup;
    }

    private PlanCategory findOrCreatePlanCategory(String categoryName) {
        return planCategoryRepository.findByCategoryName(categoryName)
                .orElseGet(() -> {
                    PlanCategory newCategory = new PlanCategory();
                    newCategory.setCategoryName(categoryName);
                    return planCategoryRepository.save(newCategory);
                });
    }

    private DataAllowances findOrCreateDataAllowances(PlanDto dto) {

        return dataAllowanceRepository.findByDataAmountAndDataUnitAndDataPeriod(
                        dto.getDataAllowance(), dto.getDataAllowanceUnit(), DataPeriod.MONTH)
                .orElseGet(() -> {
                    DataAllowances newData = new DataAllowances();
                    newData.setDataAmount(dto.getDataAllowance());
                    newData.setDataUnit(dto.getDataAllowanceUnit());
                    newData.setDataPeriod(dto.getDataPeriod());
                    return dataAllowanceRepository.save(newData);
                });
    }

    private VoiceCall findOrCreateVoiceCall(PlanDto dto) {
        return voiceCallRepository.findByVoiceAllowanceAndAdditionalCallAllowance(
                        dto.getVoiceAllowance(), dto.getAdditionalCallAllowance())
                .orElseGet(() -> {
                    VoiceCall newVoiceCall = new VoiceCall();
                    newVoiceCall.setVoiceAllowance(dto.getVoiceAllowance());
                    newVoiceCall.setAdditionalCallAllowance(dto.getAdditionalCallAllowance());
                    return voiceCallRepository.save(newVoiceCall);
                });
    }

    private SharedData findOrCreateSharedData(PlanDto dto) {
        return sharedDataRepository.findByTetheringDataAmountAndTetheringDataUnitAndFamilyDataAmountAndFamilyDataUnit(
                dto.getTetheringDataAmount(), dto.getTetheringDataUnit(), dto.getFamilyDataAmount(), dto.getFamilyDataUnit()
        ).orElseGet(() -> {
            SharedData newSharedData = new SharedData();
            newSharedData.setTetheringDataAmount(dto.getTetheringDataAmount());
            newSharedData.setTetheringDataUnit(dto.getTetheringDataUnit());
            newSharedData.setFamilyDataAmount(dto.getFamilyDataAmount());
            newSharedData.setFamilyDataUnit(dto.getFamilyDataUnit());
            newSharedData.setFamilyDataAvailable(dto.getFamilyDataAmount() != null && dto.getFamilyDataAmount() > 0);
            return sharedDataRepository.save(newSharedData);
        });
    }

    @Override
    @Transactional
    public List<PlanDto> getAllPlans() {
        List<Plan> plans = planRepository.findAllWithBasicDetails();

        return plans.stream().map(plan -> {
            Set<Integer> benefitIds = new HashSet<>();

            List<PlanBenefitGroup> planBenefitGroups = planBenefitGroupRepository.findByPlan_PlanId(plan.getPlanId());

            for (PlanBenefitGroup planBenefitGroup : planBenefitGroups) {
                List<BenefitGroupBenefit> groupBenefits =
                        benefitGroupBenefitRepository.findByBenefitGroup_BenefitGroupId(
                                planBenefitGroup.getBenefitGroup().getBenefitGroupId());

                for (BenefitGroupBenefit groupBenefit : groupBenefits) {
                    benefitIds.add(groupBenefit.getBenefit().getBenefitId().intValue());
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

    private PlanDto convertToDto(Plan plan, List<Integer> originalBenefitIds) {
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

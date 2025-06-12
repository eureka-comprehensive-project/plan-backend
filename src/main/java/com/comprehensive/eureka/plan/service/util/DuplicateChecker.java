package com.comprehensive.eureka.plan.service.util;

import com.comprehensive.eureka.plan.dto.PlanDto;
import com.comprehensive.eureka.plan.entity.Benefit;
import com.comprehensive.eureka.plan.entity.BenefitGroup;
import com.comprehensive.eureka.plan.entity.BenefitGroupBenefit;
import com.comprehensive.eureka.plan.entity.DataAllowances;
import com.comprehensive.eureka.plan.entity.PlanCategory;
import com.comprehensive.eureka.plan.entity.SharedData;
import com.comprehensive.eureka.plan.entity.VoiceCall;
import com.comprehensive.eureka.plan.entity.enums.DataPeriod;
import com.comprehensive.eureka.plan.repository.BenefitGroupBenefitRepository;
import com.comprehensive.eureka.plan.repository.BenefitGroupRepository;
import com.comprehensive.eureka.plan.repository.DataAllowanceRepository;
import com.comprehensive.eureka.plan.repository.PlanCategoryRepository;
import com.comprehensive.eureka.plan.repository.SharedDataRepository;
import com.comprehensive.eureka.plan.repository.VoiceCallRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DuplicateChecker {

    private final BenefitGroupRepository benefitGroupRepository;
    private final BenefitGroupBenefitRepository benefitGroupBenefitRepository;
    private final PlanCategoryRepository planCategoryRepository;
    private final DataAllowanceRepository dataAllowanceRepository;
    private final VoiceCallRepository voiceCallRepository;
    private final SharedDataRepository sharedDataRepository;

    public BenefitGroup findOrCreateBenefitGroupForCombination(List<Benefit> combination) {
        List<Long> combinationIds = combination.stream()
                .map(Benefit::getBenefitId)
                .sorted()
                .collect(Collectors.toList());

        return benefitGroupRepository.findBenefitGroupsByExactBenefits(combinationIds)
                .stream().findFirst().orElseGet(() -> createNewBenefitGroup(combination));
    }

    public PlanCategory findOrCreatePlanCategory(String categoryName) {
        return planCategoryRepository.findByCategoryName(categoryName)
                .orElseGet(() -> {
                    PlanCategory newCategory = new PlanCategory();
                    newCategory.setCategoryName(categoryName);
                    return planCategoryRepository.save(newCategory);
                });
    }

    public DataAllowances findOrCreateDataAllowances(PlanDto dto) {

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

    public VoiceCall findOrCreateVoiceCall(PlanDto dto) {
        return voiceCallRepository.findByVoiceAllowanceAndAdditionalCallAllowance(
                        dto.getVoiceAllowance(), dto.getAdditionalCallAllowance())
                .orElseGet(() -> {
                    VoiceCall newVoiceCall = new VoiceCall();
                    newVoiceCall.setVoiceAllowance(dto.getVoiceAllowance());
                    newVoiceCall.setAdditionalCallAllowance(dto.getAdditionalCallAllowance());
                    return voiceCallRepository.save(newVoiceCall);
                });
    }

    public SharedData findOrCreateSharedData(PlanDto dto) {
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

    public BenefitGroup createNewBenefitGroup(List<Benefit> combination) {
        String description = combination.stream()
                .map(Benefit::getBenefitName)
                .collect(Collectors.joining(" & ")) + " 조합";

        BenefitGroup newBenefitGroup = new BenefitGroup();
        newBenefitGroup.setDescription(description);
        BenefitGroup savedBenefitGroup = benefitGroupRepository.save(newBenefitGroup);

        List<BenefitGroupBenefit> groupBenefits = combination.stream()
                .map(benefit -> {
                    BenefitGroupBenefit join = new BenefitGroupBenefit();
                    join.setBenefitGroup(savedBenefitGroup);
                    join.setBenefit(benefit);
                    return join;
                })
                .collect(Collectors.toList());
        benefitGroupBenefitRepository.saveAll(groupBenefits);

        log.info("새로운 혜택 그룹 생성: {}", description);
        return savedBenefitGroup;
    }
}

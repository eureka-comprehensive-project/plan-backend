package com.comprehensive.eureka.plan.service.impl;

import com.comprehensive.eureka.plan.dto.BenefitDto;
import com.comprehensive.eureka.plan.dto.PlanBenefitDto;
import com.comprehensive.eureka.plan.dto.PlanDto;
import com.comprehensive.eureka.plan.dto.request.PlanFilterRequestDto;
import com.comprehensive.eureka.plan.dto.response.FilterListResponseDto;
import com.comprehensive.eureka.plan.dto.response.PlanFilterResponseDto;
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
    private final BenefitGroupRepository benefitGroupRepository;
    private final PlanBenefitGroupRepository planBenefitGroupRepository;

    @Override
    @Transactional
    public List<PlanDto> getAllPlans() {
        log.info("전체 요금제 조회 요청");

        List<Plan> plans = planRepository.findAllWithBenefits();

        log.debug("리포지토리에서 {}개의 요금제를 찾았습니다.", plans.size());

        return plans.stream().map(plan -> {
            List<Long> benefitIds = plan.getPlanBenefitGroups().stream()
                    .flatMap(pbg -> pbg.getBenefitGroup().getBenefitGroupBenefits().stream())
                    .map(bgb -> bgb.getBenefit().getBenefitId())
                    .distinct()
                    .collect(Collectors.toList());

            log.debug("요금제 ID {}를 DTO로 변환 중입니다. 혜택 ID: {}", plan.getPlanId(), benefitIds);
            return convertToDto(plan, benefitIds);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PlanDto getPlanById(Integer planId) {
        log.info("요금제 조회 요청: ID {}", planId);

        Plan plan = planRepository.findWithBenefitsById(planId)
                .orElseThrow( () -> new PlanException(ErrorCode.PLAN_NOT_FOUND));

        List<Long> benefitIds = plan.getPlanBenefitGroups().stream()
                .flatMap(pbg -> pbg.getBenefitGroup().getBenefitGroupBenefits().stream())
                .map(bgb -> bgb.getBenefit().getBenefitId())
                .distinct()
                .toList();

        return convertToDto(plan, new ArrayList<>(benefitIds));
    }

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

    @Override
    public List<BenefitDto> getAllBenefitsByPlanId(Integer planId) {
        log.info("plan ID: {}에 대한 getAllBenefitsByPlanId 메서드를 시작합니다.", planId);
        if (!planRepository.existsById(planId)) {
            throw new PlanException(ErrorCode.PLAN_NOT_FOUND);
        }

        List<PlanBenefitGroup> planBenefitGroups = planBenefitGroupRepository.findAllWithBenefitsByPlanId(planId);

        log.debug("요금제 ID: {}에 대한 {}개의 PlanBenefitGroup을 찾았습니다.", planId, planBenefitGroups.size());

        return planBenefitGroups.stream()
                .flatMap(pbg -> pbg.getBenefitGroup().getBenefitGroupBenefits().stream())
                .map(BenefitGroupBenefit::getBenefit)
                .distinct()
                .map(benefit -> BenefitDto.builder()
                        .benefitId(benefit.getBenefitId())
                        .benefitName(benefit.getBenefitName())
                        .benefitType(benefit.getBenefitType())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<PlanBenefitDto> getPlansByPlanBenefitIds(List<Long> planBenefitIds) {
        if (planBenefitIds == null || planBenefitIds.isEmpty()) {
            return Collections.emptyList();
        }
        log.info("PlanBenefit ID 로 요금제 조회 요청: {}", planBenefitIds);

        List<PlanBenefitGroup> results = planBenefitGroupRepository.findAllByIdWithPlan(planBenefitIds);

        return results.stream()
                .map(pbg -> PlanBenefitDto.builder()
                        .planBenefitId(pbg.getPlanBenefitId())
                        .planId(pbg.getPlan().getPlanId())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public boolean checkPlanHasBenefitGroup(Integer planId, Long benefitGroupId) {
        log.info("요금제(ID: {})가 혜택 그룹(ID: {})을 포함하는지 확인 요청", planId, benefitGroupId);

        return planBenefitGroupRepository.existsByPlan_PlanIdAndBenefitGroup_BenefitGroupId(planId, benefitGroupId);
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

        Map<Long, Benefit> benefitMap = allBenefits.stream()
                .collect(Collectors.toMap(Benefit::getBenefitId, benefit -> benefit));

        Set<Set<Long>> requiredCombinationIds = new HashSet<>();
        for (Benefit benefit : allBenefits) {
            requiredCombinationIds.add(Set.of(benefit.getBenefitId()));
        }

        Map<BenefitType, List<Benefit>> benefitsByType = allBenefits.stream()
                .collect(Collectors.groupingBy(Benefit::getBenefitType));

        List<BenefitType> combinableTypes = benefitsByType.keySet().stream()
                .filter(type -> type != BenefitType.BASIC)
                .toList();

        for (int i = 0; i < combinableTypes.size(); i++) {
            for (int j = i + 1; j < combinableTypes.size(); j++) {
                List<Benefit> list1 = benefitsByType.get(combinableTypes.get(i));
                List<Benefit> list2 = benefitsByType.get(combinableTypes.get(j));
                for (Benefit benefit1 : list1) {
                    for (Benefit benefit2 : list2) {
                        requiredCombinationIds.add(Set.of(benefit1.getBenefitId(), benefit2.getBenefitId()));
                    }
                }
            }
        }

        Map<Set<Long>, BenefitGroup> existingGroupsMap = benefitGroupRepository
                .findByBenefitIdsWithBenefits(new HashSet<>(benefitIdList)).stream()
                .collect(Collectors.toMap(
                        group -> group.getBenefitGroupBenefits().stream()
                                .map(bgb -> bgb.getBenefit().getBenefitId())
                                .collect(Collectors.toSet()),
                        group -> group,
                        (existing, replacement) -> existing
                ));

        List<BenefitGroup> resultGroups = new ArrayList<>();

        for (Set<Long> combinationIds : requiredCombinationIds) {
            BenefitGroup group = existingGroupsMap.get(combinationIds);

            if (group != null) {
                resultGroups.add(group);
            } else {
                BenefitGroup newGroup = createNewBenefitGroup(combinationIds, benefitMap);
                benefitGroupRepository.save(newGroup);
                resultGroups.add(newGroup);

                existingGroupsMap.put(combinationIds, newGroup);
            }
        }

        return resultGroups;
    }

    private BenefitGroup createNewBenefitGroup(Set<Long> combinationIds, Map<Long, Benefit> benefitMap) {
        log.info("새로운 혜택 그룹 생성. 혜택 ID: {}", combinationIds);

        List<Benefit> combinationBenefits = combinationIds.stream()
                .map(benefitMap::get)
                .toList();

        BenefitGroup newGroup = new BenefitGroup();
        newGroup.setDescription(combinationBenefits.stream()
                .map(Benefit::getBenefitName)
                .sorted()
                .collect(Collectors.joining(", ")));

        Set<BenefitGroupBenefit> bgbSet = combinationBenefits.stream().map(benefit -> {
            BenefitGroupBenefit bgb = new BenefitGroupBenefit();
            bgb.setBenefitGroup(newGroup);
            bgb.setBenefit(benefit);
            return bgb;
        }).collect(Collectors.toSet());

        newGroup.setBenefitGroupBenefits(bgbSet);
        return newGroup;
    }

    private PlanDto convertToDto(Plan plan, List<Long> originalBenefitIds) {
        return PlanDto.builder()
                .planId(plan.getPlanId())
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

    public List<PlanFilterResponseDto> getFilteredPlans(PlanFilterRequestDto filterRequest) {
        log.info("getFilteredPlans 메서드를 시작합니다. 필터 요청: {}", filterRequest);
        List<Plan> plans = planRepository.findPlansWithFilter(filterRequest);

        log.debug("필터링된 요금제 {}개를 찾았습니다.", plans.size());

        return plans.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private PlanFilterResponseDto convertToResponse(Plan plan) {
        log.debug("Plan 객체 (ID: {})를 PlanFilterResponseDto로 변환합니다.", plan.getPlanId());
        return PlanFilterResponseDto.builder()
                .planId(plan.getPlanId())
                .planName(plan.getPlanName())
                .monthlyFee(plan.getMonthlyFee())
                .categoryName(plan.getPlanCategory() != null ? plan.getPlanCategory().getCategoryName() : null)
                .dataAllowance(convertDataAllowance(plan))
                .voiceCall(convertVoiceCall(plan))
                .sharedData(convertSharedData(plan))
                .benefits(convertBenefits(plan))
                .build();
    }

    private PlanFilterResponseDto.DataAllowanceInfo convertDataAllowance(Plan plan) {
        log.trace("Plan ID: {}에 대한 DataAllowanceInfo를 변환합니다.", plan.getPlanId());
        if (plan.getDataAllowances() == null) {
            log.debug("Plan ID: {}에 대한 DataAllowances가 null입니다.", plan.getPlanId());
            return null;
        }

        return PlanFilterResponseDto.DataAllowanceInfo.builder()
                .dataAmount(plan.getDataAllowances().getDataAmount())
                .dataUnit(plan.getDataAllowances().getDataUnit())
                .dataPeriod(plan.getDataAllowances().getDataPeriod().name())
                .build();
    }

    private PlanFilterResponseDto.VoiceCallInfo convertVoiceCall(Plan plan) {
        log.trace("Plan ID: {}에 대한 VoiceCallInfo를 변환합니다.", plan.getPlanId());
        if (plan.getVoiceCall() == null) {
            log.debug("Plan ID: {}에 대한 VoiceCall이 null입니다.", plan.getPlanId());
            return null;
        }

        return PlanFilterResponseDto.VoiceCallInfo.builder()
                .voiceAllowance(plan.getVoiceCall().getVoiceAllowance())
                .additionalCallAllowance(plan.getVoiceCall().getAdditionalCallAllowance())
                .build();
    }

    private PlanFilterResponseDto.SharedDataInfo convertSharedData(Plan plan) {
        log.trace("Plan ID: {}에 대한 SharedDataInfo를 변환합니다.", plan.getPlanId());
        if (plan.getSharedData() == null) {
            log.debug("Plan ID: {}에 대한 SharedData가 null입니다.", plan.getPlanId());
            return null;
        }

        return PlanFilterResponseDto.SharedDataInfo.builder()
                .tetheringDataAmount(plan.getSharedData().getTetheringDataAmount())
                .tetheringDataUnit(plan.getSharedData().getTetheringDataUnit())
                .familyDataAvailable(plan.getSharedData().getFamilyDataAvailable())
                .familyDataAmount(plan.getSharedData().getFamilyDataAmount())
                .familyDataUnit(plan.getSharedData().getFamilyDataUnit())
                .build();
    }

    private List<PlanFilterResponseDto.BenefitInfo> convertBenefits(Plan plan) {
        log.trace("Plan ID: {}에 대한 BenefitInfo 목록을 변환합니다.", plan.getPlanId());
        if (plan.getPlanBenefitGroups() == null) {
            log.debug("Plan ID: {}에 대한 PlanBenefitGroups가 null입니다. 빈 목록을 반환합니다.", plan.getPlanId());
            return List.of();
        }

        return plan.getPlanBenefitGroups().stream()
                .filter(pbg -> pbg.getBenefitGroup() != null && pbg.getBenefitGroup().getBenefitGroupBenefits() != null)
                .flatMap(pbg -> pbg.getBenefitGroup().getBenefitGroupBenefits().stream())
                .filter(bgb -> bgb.getBenefit() != null)
                .map(bgb -> PlanFilterResponseDto.BenefitInfo.builder()
                        .benefitId(bgb.getBenefit().getBenefitId())
                        .benefitName(bgb.getBenefit().getBenefitName())
                        .benefitType(bgb.getBenefit().getBenefitType().name())
                        .build())
                .distinct()
                .collect(Collectors.toList());
    }

    public int countPlansWithFilter(PlanFilterRequestDto requestDto) {
        log.info("countPlansWithFilter 메서드를 시작합니다. 필터 요청: {}", requestDto);
        return planRepository.countPlansWithFilter(requestDto);
    }

    @Override
    @Transactional
    public List<PlanDto> findPlansByPlanNameContaining(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllPlans();
        }

        List<Plan> plans = planRepository.findByPlanNameContainingIgnoreCase(searchTerm);

        return plans.stream()
                .map(this::convertToDtoWithBenefits)
                .collect(Collectors.toList());
    }

    private PlanDto convertToDtoWithBenefits(Plan plan) {
        return PlanDto.builder()
                .planId(plan.getPlanId())
                .planName(plan.getPlanName())
                .planCategory(plan.getPlanCategory() != null ? plan.getPlanCategory().getCategoryName() : null)
                .monthlyFee(plan.getMonthlyFee())
                .dataAllowance(plan.getDataAllowances() != null ? plan.getDataAllowances().getDataAmount() : null)
                .dataAllowanceUnit(plan.getDataAllowances() != null ? plan.getDataAllowances().getDataUnit() : null)
                .dataPeriod(plan.getDataAllowances() != null && plan.getDataAllowances().getDataPeriod() != null ? DataPeriod.valueOf(plan.getDataAllowances().getDataPeriod().name()) : null)
                .tetheringDataAmount(plan.getSharedData() != null ? plan.getSharedData().getTetheringDataAmount() : null)
                .tetheringDataUnit(plan.getSharedData() != null ? plan.getSharedData().getTetheringDataUnit() : null)
                .familyDataAmount(plan.getSharedData() != null ? plan.getSharedData().getFamilyDataAmount() : null)
                .familyDataUnit(plan.getSharedData() != null ? plan.getSharedData().getFamilyDataUnit() : null)
                .voiceAllowance(plan.getVoiceCall() != null ? plan.getVoiceCall().getVoiceAllowance() : null)
                .additionalCallAllowance(plan.getVoiceCall() != null ? plan.getVoiceCall().getAdditionalCallAllowance() : null)
                .benefitIdList(plan.getPlanBenefitGroups().stream()
                        .flatMap(pbg -> pbg.getBenefitGroup().getBenefitGroupBenefits().stream())
                        .map(bgb -> bgb.getBenefit().getBenefitId())
                        .distinct()
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public List<FilterListResponseDto> getFilteredList(PlanFilterRequestDto filterRequest) {
        log.info("getFilteredList 메서드를 시작합니다. 필터 요청: {}", filterRequest);
        return planRepository.getFilteredList(filterRequest);
    }
}

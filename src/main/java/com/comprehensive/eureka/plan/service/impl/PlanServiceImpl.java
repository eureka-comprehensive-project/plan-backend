package com.comprehensive.eureka.plan.service.impl;

import com.comprehensive.eureka.plan.dto.BenefitDto;
import com.comprehensive.eureka.plan.dto.PlanBenefitDto;
import com.comprehensive.eureka.plan.dto.PlanDto;
import com.comprehensive.eureka.plan.dto.request.PlanFilterRequestDto;
import com.comprehensive.eureka.plan.dto.response.FilterListResponseDto;
import com.comprehensive.eureka.plan.dto.response.PlanFilterResponseDto;
import com.comprehensive.eureka.plan.entity.*;
import com.comprehensive.eureka.plan.entity.enums.BenefitType;
import com.comprehensive.eureka.plan.entity.enums.DataPeriod;
import com.comprehensive.eureka.plan.exception.ErrorCode;
import com.comprehensive.eureka.plan.exception.PlanException;
import com.comprehensive.eureka.plan.repository.BenefitGroupRepository;
import com.comprehensive.eureka.plan.repository.BenefitRepository;
import com.comprehensive.eureka.plan.repository.PlanBenefitGroupRepository;
import com.comprehensive.eureka.plan.repository.PlanRepository;
import com.comprehensive.eureka.plan.service.PlanService;
import com.comprehensive.eureka.plan.service.util.DuplicateChecker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new PlanException(ErrorCode.PLAN_NOT_FOUND));

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

    // 가상 클래스 및 import 구문은 생략합니다.

    @Transactional
    public List<BenefitGroup> processBenefitCombinations(List<Long> benefitIdList) {
        if (benefitIdList == null || benefitIdList.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 요청된 ID에 해당하는 모든 Benefit 엔티티를 조회합니다.
        List<Benefit> allBenefits = benefitRepository.findAllById(benefitIdList);
        if (allBenefits.size() != benefitIdList.size()) {
            log.warn("요청된 혜택 ID 중 일부를 찾을 수 없습니다. 요청 ID: {}", benefitIdList);
            // 실제 환경에서는 적절한 예외 처리가 필요합니다.
            // throw new EntityNotFoundException("Benefit not found for some IDs.");
        }

        // 빠른 조회를 위해 Benefit ID를 키로 하는 Map을 생성합니다.
        Map<Long, Benefit> benefitMap = allBenefits.stream()
                .collect(Collectors.toMap(Benefit::getBenefitId, benefit -> benefit));

        // 혜택을 타입(BASIC, PREMIUM 등)별로 그룹화합니다.
        Map<BenefitType, List<Benefit>> benefitsByType = allBenefits.stream()
                .collect(Collectors.groupingBy(Benefit::getBenefitType));

        List<BenefitType> types = new ArrayList<>(benefitsByType.keySet());
        Set<Set<Long>> requiredCombinationIds = new HashSet<>();

        // 2. 재귀 함수를 호출하여 필요한 모든 혜택 조합(ID Set)을 생성합니다.
        findCombinations(0, new ArrayDeque<>(), types, benefitsByType, requiredCombinationIds);

        // 3. 기존에 생성된 BenefitGroup들을 한 번에 조회하여 Map으로 만듭니다.
        // Key: 그룹에 포함된 혜택 ID Set, Value: BenefitGroup 엔티티
        Map<Set<Long>, BenefitGroup> existingGroupsMap = benefitGroupRepository
                .findAllByBenefitGroupBenefitsBenefitBenefitIdIn(new HashSet<>(benefitIdList))
                .stream()
                .collect(Collectors.toMap(
                        group -> group.getBenefitGroupBenefits().stream()
                                .map(bgb -> bgb.getBenefit().getBenefitId())
                                .collect(Collectors.toSet()),
                        group -> group,
                        (existing, replacement) -> existing
                ));

        // 4. 각 조합에 대해 BenefitGroup이 존재하면 사용하고, 없으면 새로 생성합니다.
        List<BenefitGroup> resultGroups = new ArrayList<>();
        for (Set<Long> combinationIds : requiredCombinationIds) {
            BenefitGroup group = existingGroupsMap.computeIfAbsent(combinationIds, ids -> {
                BenefitGroup newGroup = createNewBenefitGroup(ids, benefitMap);
                return benefitGroupRepository.save(newGroup); // 새 그룹을 DB에 저장
            });
            resultGroups.add(group);
        }

        return resultGroups;
    }

    /**
     * 백트래킹을 사용하여 혜택 조합을 생성하는 재귀 함수입니다.
     * @param startIndex         탐색을 시작할 혜택 타입의 인덱스 (중복 조합 방지)
     * @param currentCombination 현재 경로에서 만들어진 조합 (ID Deque)
     * @param types              전체 혜택 타입 리스트
     * @param benefitsByType     타입별로 그룹화된 혜택 맵
     * @param allCombinations    생성된 모든 조합(ID Set)을 저장할 최종 Set
     */
    private void findCombinations(
            int startIndex,
            Deque<Long> currentCombination,
            List<BenefitType> types,
            Map<BenefitType, List<Benefit>> benefitsByType,
            Set<Set<Long>> allCombinations) {

        // startIndex부터 시작하여 각 혜택 타입을 순회합니다.
        for (int i = startIndex; i < types.size(); i++) {
            BenefitType currentType = types.get(i);
            List<Benefit> benefitsInType = benefitsByType.get(currentType);

            // 현재 타입에 속한 각 혜택을 조합에 추가하는 경우를 탐색합니다.
            for (Benefit benefit : benefitsInType) {
                // 1. 조합에 현재 혜택 ID를 추가합니다.
                currentCombination.addLast(benefit.getBenefitId());

                // 2. 현재까지 만들어진 조합을 결과 Set에 저장합니다.
                allCombinations.add(new HashSet<>(currentCombination));

                // 3. 다음 타입(i+1)으로 넘어가 하위 조합을 찾기 위해 재귀 호출합니다.
                findCombinations(i + 1, currentCombination, types, benefitsByType, allCombinations);

                // 4. 백트래킹: 탐색이 끝났으므로 다음 경우의 수를 위해 마지막에 추가했던 혜택 ID를 제거합니다.
                currentCombination.removeLast();
            }
        }
    }

    /**
     * 새로운 BenefitGroup 엔티티와 그에 속한 BenefitGroupBenefit 관계 엔티티들을 생성합니다.
     * @param combinationIds 생성할 그룹에 포함될 혜택 ID Set
     * @param benefitMap     ID로 Benefit 엔티티를 빠르게 찾기 위한 맵
     * @return 저장 준비가 완료된 새로운 BenefitGroup 엔티티
     */
    private BenefitGroup createNewBenefitGroup(Set<Long> combinationIds, Map<Long, Benefit> benefitMap) {
        BenefitGroup newGroup = new BenefitGroup();
        newGroup.setDescription("Generated Group"); // 필요시 설명 추가

        // 혜택 ID들을 기반으로 BenefitGroupBenefit(중간 테이블 엔티티) Set을 생성합니다.
        Set<BenefitGroupBenefit> benefitGroupBenefits = combinationIds.stream()
                .map(benefitId -> {
                    Benefit benefit = benefitMap.get(benefitId);
                    BenefitGroupBenefit bgb = new BenefitGroupBenefit();
                    bgb.setBenefitGroup(newGroup); // 관계 설정 (자식 -> 부모)
                    bgb.setBenefit(benefit);       // 관계 설정 (자식 -> 혜택)
                    return bgb;
                })
                .collect(Collectors.toSet());

        // 생성된 중간 엔티티 Set을 BenefitGroup에 설정합니다. (부모 -> 자식)
        // CascadeType.ALL 옵션 덕분에 부모(newGroup)만 저장하면 자식(bgb)들도 함께 저장됩니다.
        newGroup.setBenefitGroupBenefits(benefitGroupBenefits);

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

    @Override
    public Long getBenefitGroupsByPlanIds(List<Long> benefitIds) {
        return planRepository.findBenefitGroupIdsByAllBenefitIds(benefitIds)
                .map(BenefitGroup::getBenefitGroupId)
                .orElse(null);
    }
}

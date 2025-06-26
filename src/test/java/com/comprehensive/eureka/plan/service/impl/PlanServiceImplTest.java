package com.comprehensive.eureka.plan.service.impl;

import com.comprehensive.eureka.plan.dto.BenefitDto;
import com.comprehensive.eureka.plan.dto.PlanBenefitDto;
import com.comprehensive.eureka.plan.dto.PlanDto;
import com.comprehensive.eureka.plan.dto.request.GetPlanBenefitGroupIdRequestDto;
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
import com.comprehensive.eureka.plan.repository.PlanCategoryRepository;
import com.comprehensive.eureka.plan.repository.DataAllowanceRepository;
import com.comprehensive.eureka.plan.repository.VoiceCallRepository;
import com.comprehensive.eureka.plan.repository.SharedDataRepository;
import com.comprehensive.eureka.plan.repository.PlanRepository;
import com.comprehensive.eureka.plan.service.util.DuplicateChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlanServiceImpl 단위 테스트")
class PlanServiceImplTest {

    @Mock
    private PlanRepository planRepository;
    @Mock
    private PlanCategoryRepository planCategoryRepository;
    @Mock
    private DataAllowanceRepository dataAllowanceRepository;
    @Mock
    private VoiceCallRepository voiceCallRepository;
    @Mock
    private SharedDataRepository sharedDataRepository;
    @Mock
    private PlanBenefitGroupRepository planBenefitGroupRepository;
    @Mock
    private BenefitRepository benefitRepository;
    @Mock
    private BenefitGroupRepository benefitGroupRepository;
    @Mock
    private DuplicateChecker duplicateChecker;

    @InjectMocks
    private PlanServiceImpl planService;

    private Plan mockPlan;
    private PlanCategory mockCategory;
    private DataAllowances mockDataAllowances;
    private VoiceCall mockVoiceCall;
    private SharedData mockSharedData;

    private Benefit mockBenefit1;
    private Benefit mockBenefit2;
    private Benefit mockNewBenefit;

    private BenefitGroup mockBenefitGroup1;
    private BenefitGroup mockBenefitGroup2;
    private BenefitGroup mockNewBenefitGroup;

    private PlanBenefitGroup mockPlanBenefitGroup1;
    private PlanBenefitGroup mockPlanBenefitGroup2;
    private PlanBenefitGroup mockNewPlanBenefitGroup;

    @BeforeEach
    void setUp() {
        mockCategory = PlanCategory.builder().categoryId(1L).categoryName("5G").plans(new ArrayList<>()).build();
        mockDataAllowances = DataAllowances.builder().dataAllowanceId(1L).dataAmount(10).dataUnit("GB").dataPeriod(DataPeriod.MONTH).plans(new ArrayList<>()).build();
        mockVoiceCall = VoiceCall.builder().voiceCallId(1L).voiceAllowance(100).additionalCallAllowance(50).plans(new ArrayList<>()).build();
        mockSharedData = SharedData.builder().sharedDataId(1L).tetheringDataAmount(5).tetheringDataUnit("GB").familyDataAvailable(true).familyDataAmount(2).familyDataUnit("GB").plans(new ArrayList<>()).build();

        mockBenefit1 = Benefit.builder().benefitId(1L).benefitName("Youtube Premium").benefitType(BenefitType.MEDIA).benefitGroupBenefits(new ArrayList<>()).build();
        mockBenefit2 = Benefit.builder().benefitId(2L).benefitName("Melon Streaming").benefitType(BenefitType.MEDIA).benefitGroupBenefits(new ArrayList<>()).build();

        mockNewBenefit = Benefit.builder().benefitId(3L).benefitName("New Streaming Service").benefitType(BenefitType.MEDIA).benefitGroupBenefits(new ArrayList<>()).build();


        mockBenefitGroup1 = BenefitGroup.builder()
                .benefitGroupId(10L)
                .description("Media Group 1 Description")
                .benefitGroupBenefits(new HashSet<>())
                .planBenefitGroups(new ArrayList<>())
                .build();
        mockBenefitGroup2 = BenefitGroup.builder()
                .benefitGroupId(20L)
                .description("Media Group 2 Description")
                .benefitGroupBenefits(new HashSet<>())
                .planBenefitGroups(new ArrayList<>())
                .build();

        mockNewBenefitGroup = BenefitGroup.builder()
                .benefitGroupId(30L)
                .description("New Media Group Description")
                .benefitGroupBenefits(new HashSet<>())
                .planBenefitGroups(new ArrayList<>())
                .build();

        BenefitGroupBenefit bgb1_1 = BenefitGroupBenefit.builder()
                .benefitGroupBenefitId(101L)
                .benefitGroup(mockBenefitGroup1)
                .benefit(mockBenefit1)
                .build();
        mockBenefitGroup1.getBenefitGroupBenefits().add(bgb1_1);
        mockBenefit1.getBenefitGroupBenefits().add(bgb1_1);

        BenefitGroupBenefit bgb2_1 = BenefitGroupBenefit.builder()
                .benefitGroupBenefitId(102L)
                .benefitGroup(mockBenefitGroup2)
                .benefit(mockBenefit2)
                .build();
        mockBenefitGroup2.getBenefitGroupBenefits().add(bgb2_1);
        mockBenefit2.getBenefitGroupBenefits().add(bgb2_1);

        BenefitGroupBenefit bgbNew = BenefitGroupBenefit.builder()
                .benefitGroupBenefitId(103L)
                .benefitGroup(mockNewBenefitGroup)
                .benefit(mockNewBenefit)
                .build();
        mockNewBenefitGroup.getBenefitGroupBenefits().add(bgbNew);
        mockNewBenefit.getBenefitGroupBenefits().add(bgbNew);

        mockPlan = Plan.builder()
                .planId(1L)
                .planName("Test Plan")
                .monthlyFee(50000)
                .planCategory(mockCategory)
                .dataAllowances(mockDataAllowances)
                .voiceCall(mockVoiceCall)
                .sharedData(mockSharedData)
                .planBenefitGroups(new HashSet<>())
                .build();

        mockPlanBenefitGroup1 = PlanBenefitGroup.builder()
                .planBenefitId(1L)
                .plan(mockPlan)
                .benefitGroup(mockBenefitGroup1)
                .build();
        mockPlanBenefitGroup2 = PlanBenefitGroup.builder()
                .planBenefitId(2L)
                .plan(mockPlan)
                .benefitGroup(mockBenefitGroup2)
                .build();
        mockNewPlanBenefitGroup = PlanBenefitGroup.builder()
                .planBenefitId(3L)
                .plan(mockPlan)
                .benefitGroup(mockNewBenefitGroup)
                .build();

        mockPlan.getPlanBenefitGroups().add(mockPlanBenefitGroup1);
        mockPlan.getPlanBenefitGroups().add(mockPlanBenefitGroup2);

        mockBenefitGroup1.getPlanBenefitGroups().add(mockPlanBenefitGroup1);
        mockBenefitGroup2.getPlanBenefitGroups().add(mockPlanBenefitGroup2);

        PlanDto mockPlanDto = PlanDto.builder()
                .planId(mockPlan.getPlanId().intValue())
                .planName("Test Plan")
                .monthlyFee(50000)
                .planCategory("5G")
                .dataAllowance(10)
                .dataAllowanceUnit("GB")
                .dataPeriod(DataPeriod.MONTH)
                .tetheringDataAmount(5)
                .tetheringDataUnit("GB")
                .familyDataAmount(2)
                .familyDataUnit("GB")
                .voiceAllowance(100)
                .additionalCallAllowance(50)
                .benefitIdList(Arrays.asList(mockBenefit1.getBenefitId(), mockBenefit2.getBenefitId()))
                .build();
    }

    // --- getAllPlans 테스트 ---
    @Test
    @DisplayName("모든 요금제 조회 (혜택 포함)")
    void getAllPlans_ReturnsListOfPlanDtosWithBenefits() {
        // Given
        when(planRepository.findAllWithBenefits()).thenReturn(Collections.singletonList(mockPlan));

        // When
        List<PlanDto> result = planService.getAllPlans();

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        PlanDto resultDto = result.get(0);
        assertEquals(mockPlan.getPlanId().intValue(), resultDto.getPlanId());
        assertEquals(mockPlan.getPlanName(), resultDto.getPlanName());
        assertEquals(mockPlan.getMonthlyFee(), resultDto.getMonthlyFee());
        assertNotNull(resultDto.getBenefitIdList());
        assertEquals(2, resultDto.getBenefitIdList().size());
        assertTrue(resultDto.getBenefitIdList().containsAll(Arrays.asList(mockBenefit1.getBenefitId(), mockBenefit2.getBenefitId())));

        verify(planRepository, times(1)).findAllWithBenefits();
    }

    @Test
    @DisplayName("요금제 없을 때 빈 목록 반환")
    void getAllPlans_NoPlansFound_ReturnsEmptyList() {
        // Given
        when(planRepository.findAllWithBenefits()).thenReturn(Collections.emptyList());

        // When
        List<PlanDto> result = planService.getAllPlans();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(planRepository, times(1)).findAllWithBenefits();
    }

    // --- getPlanById 테스트 ---
    @Test
    @DisplayName("유효 ID로 요금제 조회")
    void getPlanById_ValidId_ReturnsPlanDto() {
        // Given
        Integer planIdAsInteger = mockPlan.getPlanId().intValue();
        when(planRepository.findWithBenefitsById(planIdAsInteger)).thenReturn(Optional.of(mockPlan));

        // When
        PlanDto result = planService.getPlanById(planIdAsInteger);

        // Then
        assertNotNull(result);
        assertEquals(planIdAsInteger, result.getPlanId());
        assertEquals(mockPlan.getPlanName(), result.getPlanName());
        assertNotNull(result.getBenefitIdList());
        assertEquals(2, result.getBenefitIdList().size());
        assertTrue(result.getBenefitIdList().containsAll(Arrays.asList(mockBenefit1.getBenefitId(), mockBenefit2.getBenefitId())));

        verify(planRepository, times(1)).findWithBenefitsById(planIdAsInteger);
    }

    @Test
    @DisplayName("존재하지 않는 ID 조회 시 예외 발생")
    void getPlanById_InvalidId_ThrowsPlanNotFoundException() {
        // Given
        Integer invalidPlanId = 999;
        when(planRepository.findWithBenefitsById(invalidPlanId)).thenReturn(Optional.empty());

        // When & Then
        PlanException exception = assertThrows(PlanException.class, () -> planService.getPlanById(invalidPlanId));
        assertEquals(ErrorCode.PLAN_NOT_FOUND, exception.getErrorCode());
        verify(planRepository, times(1)).findWithBenefitsById(invalidPlanId);
    }

    // --- createPlan 테스트 ---
    @Test
    @DisplayName("요금제 생성 성공")
    void createPlan_Success() {
        // Given
        PlanDto newPlanDto = PlanDto.builder()
                .planName("New Plan")
                .monthlyFee(30000)
                .planCategory("4G")
                .dataAllowance(5)
                .dataAllowanceUnit("GB")
                .dataPeriod(DataPeriod.MONTH)
                .voiceAllowance(50)
                .additionalCallAllowance(10)
                .tetheringDataAmount(1)
                .tetheringDataUnit("GB")
                .familyDataAmount(0)
                .familyDataUnit("GB")
                .benefitIdList(Collections.singletonList(mockBenefit1.getBenefitId()))
                .build();

        Plan savedPlan = Plan.builder()
                .planId(2L)
                .planName(newPlanDto.getPlanName())
                .monthlyFee(newPlanDto.getMonthlyFee())
                .planCategory(mockCategory)
                .dataAllowances(mockDataAllowances)
                .voiceCall(mockVoiceCall)
                .sharedData(mockSharedData)
                .planBenefitGroups(new HashSet<>())
                .build();

        when(duplicateChecker.findOrCreatePlanCategory(anyString())).thenReturn(mockCategory);
        when(duplicateChecker.findOrCreateDataAllowances(any(PlanDto.class))).thenReturn(mockDataAllowances);
        when(duplicateChecker.findOrCreateVoiceCall(any(PlanDto.class))).thenReturn(mockVoiceCall);
        when(duplicateChecker.findOrCreateSharedData(any(PlanDto.class))).thenReturn(mockSharedData);

        when(planRepository.existsByPlanName(newPlanDto.getPlanName())).thenReturn(false);
        when(planRepository.save(any(Plan.class))).thenReturn(savedPlan);

        when(benefitRepository.findAllById(anyIterable())).thenReturn(Arrays.asList(mockBenefit1));

        Set<Long> benefitIdsForMockBenefit1Group = mockBenefitGroup1.getBenefitGroupBenefits().stream()
                .map(BenefitGroupBenefit::getBenefit)
                .map(Benefit::getBenefitId)
                .collect(Collectors.toSet());
        when(benefitGroupRepository.save(any(BenefitGroup.class))).thenReturn(mockBenefitGroup1);

        // When
        PlanDto result = planService.createPlan(newPlanDto);

        // Then
        assertNotNull(result);
        assertEquals(savedPlan.getPlanId().intValue(), result.getPlanId());
        assertEquals(newPlanDto.getPlanName(), result.getPlanName());
        assertEquals(newPlanDto.getMonthlyFee(), result.getMonthlyFee());
        assertNotNull(result.getBenefitIdList());
        assertEquals(1, result.getBenefitIdList().size());
        assertTrue(result.getBenefitIdList().contains(mockBenefit1.getBenefitId()));

        verify(planRepository, times(1)).existsByPlanName(newPlanDto.getPlanName());
        verify(planRepository, times(1)).save(any(Plan.class));

        ArgumentCaptor<List<PlanBenefitGroup>> savedPlanBenefitGroupsCaptor = ArgumentCaptor.forClass(List.class);
        verify(planBenefitGroupRepository, times(1)).saveAll(savedPlanBenefitGroupsCaptor.capture());
        List<PlanBenefitGroup> savedPlanBenefitGroups = savedPlanBenefitGroupsCaptor.getValue();
        assertEquals(1, savedPlanBenefitGroups.size());
        assertEquals(mockBenefitGroup1.getBenefitGroupId(), savedPlanBenefitGroups.get(0).getBenefitGroup().getBenefitGroupId());


        verify(duplicateChecker, times(1)).findOrCreatePlanCategory(anyString());
        verify(duplicateChecker, times(1)).findOrCreateDataAllowances(any(PlanDto.class));
        verify(duplicateChecker, times(1)).findOrCreateVoiceCall(any(PlanDto.class));
        verify(duplicateChecker, times(1)).findOrCreateSharedData(any(PlanDto.class));
    }

    @Test
    @DisplayName("중복 이름 생성 시 예외 발생")
    void createPlan_AlreadyExistsName_ThrowsPlanAlreadyExistsException() {
        // Given
        PlanDto existingPlanDto = PlanDto.builder().planName("Existing Plan").build();
        when(planRepository.existsByPlanName(existingPlanDto.getPlanName())).thenReturn(true);

        // When & Then
        PlanException exception = assertThrows(PlanException.class, () -> planService.createPlan(existingPlanDto));
        assertEquals(ErrorCode.PLAN_ALREADY_EXISTS, exception.getErrorCode());
        verify(planRepository, times(1)).existsByPlanName(existingPlanDto.getPlanName());
        verify(planRepository, never()).save(any(Plan.class));
    }

    @Test
    @DisplayName("생성 중 오류 발생 시 예외 발생")
    void createPlan_UnexpectedError_ThrowsPlanCreateFailureException() {
        // Given
        PlanDto newPlanDto = PlanDto.builder().planName("Error Plan").build();
        when(planRepository.existsByPlanName(newPlanDto.getPlanName())).thenReturn(false);
        when(duplicateChecker.findOrCreatePlanCategory(anyString())).thenThrow(new RuntimeException("DB Error"));

        // When & Then
        PlanException exception = assertThrows(PlanException.class, () -> planService.createPlan(newPlanDto));
        assertEquals(ErrorCode.PLAN_CREATE_FAILURE, exception.getErrorCode());
        verify(planRepository, times(1)).existsByPlanName(newPlanDto.getPlanName());
        verify(planRepository, never()).save(any(Plan.class));
    }


    // --- updatePlan 테스트 ---
    @Test
    @DisplayName("요금제 업데이트 성공")
    void updatePlan_Success() {
        // Given
        Integer planIdAsInteger = mockPlan.getPlanId().intValue();

        PlanDto updatedPlanDto = PlanDto.builder()
                .planName("Updated Plan Name")
                .monthlyFee(60000)
                .planCategory("5G")
                .dataAllowance(20)
                .dataAllowanceUnit("GB")
                .dataPeriod(DataPeriod.MONTH)
                .voiceAllowance(200)
                .additionalCallAllowance(20)
                .tetheringDataAmount(10)
                .tetheringDataUnit("GB")
                .familyDataAmount(5)
                .familyDataUnit("GB")
                .benefitIdList(Arrays.asList(mockBenefit1.getBenefitId(), mockNewBenefit.getBenefitId()))
                .build();


        when(planRepository.findById(Math.toIntExact(mockPlan.getPlanId()))).thenReturn(Optional.of(mockPlan));
        when(planRepository.findByPlanName(updatedPlanDto.getPlanName())).thenReturn(Optional.empty());

        when(duplicateChecker.findOrCreatePlanCategory(anyString())).thenReturn(mockCategory);
        when(duplicateChecker.findOrCreateDataAllowances(any(PlanDto.class))).thenReturn(mockDataAllowances);
        when(duplicateChecker.findOrCreateVoiceCall(any(PlanDto.class))).thenReturn(mockVoiceCall);
        when(duplicateChecker.findOrCreateSharedData(any(PlanDto.class))).thenReturn(mockSharedData);


        List<PlanBenefitGroup> existingPlanBenefitsList = new ArrayList<>(mockPlan.getPlanBenefitGroups());
        when(planBenefitGroupRepository.findAllByPlan(mockPlan)).thenReturn(existingPlanBenefitsList);

        Set<Long> requestedBenefitIds = new HashSet<>(updatedPlanDto.getBenefitIdList());

        when(benefitRepository.findAllById(anyIterable())).thenReturn(Arrays.asList(mockBenefit1, mockNewBenefit));

        when(benefitGroupRepository.findAllByBenefitGroupBenefitsBenefitBenefitIdIn(requestedBenefitIds))
                .thenReturn(Arrays.asList(mockBenefitGroup1, mockNewBenefitGroup));
        when(benefitRepository.findAllById(anyIterable())).thenReturn(Arrays.asList(mockBenefit1, mockNewBenefit));


        // When
        PlanDto result = planService.updatePlan(planIdAsInteger, updatedPlanDto);

        // Then
        assertNotNull(result);
        assertEquals(updatedPlanDto.getPlanName(), result.getPlanName());
        assertEquals(updatedPlanDto.getMonthlyFee(), result.getMonthlyFee());
        assertEquals(2, result.getBenefitIdList().size());
        assertTrue(result.getBenefitIdList().contains(mockBenefit1.getBenefitId()));
        assertTrue(result.getBenefitIdList().contains(mockNewBenefit.getBenefitId()));
        assertFalse(result.getBenefitIdList().contains(mockBenefit2.getBenefitId()));

        // Verify
        verify(planRepository, times(1)).findById(Math.toIntExact(mockPlan.getPlanId()));
        verify(planRepository, times(1)).findByPlanName(updatedPlanDto.getPlanName());
        verify(duplicateChecker, times(1)).findOrCreatePlanCategory(anyString());
        verify(duplicateChecker, times(1)).findOrCreateDataAllowances(any(PlanDto.class));
        verify(duplicateChecker, times(1)).findOrCreateVoiceCall(any(PlanDto.class));
        verify(duplicateChecker, times(1)).findOrCreateSharedData(any(PlanDto.class));

        ArgumentCaptor<List<PlanBenefitGroup>> deletedPbgCaptor = ArgumentCaptor.forClass(List.class);
        verify(planBenefitGroupRepository, times(1)).deleteAll(deletedPbgCaptor.capture());
        List<PlanBenefitGroup> deletedList = deletedPbgCaptor.getValue();
        assertEquals(1, deletedList.size());
        assertTrue(deletedList.stream().anyMatch(pbg ->
                pbg.getPlanBenefitId().equals(mockPlanBenefitGroup2.getPlanBenefitId())
        ));

        ArgumentCaptor<List<PlanBenefitGroup>> savedPbgCaptor = ArgumentCaptor.forClass(List.class);
        verify(planBenefitGroupRepository, times(1)).saveAll(savedPbgCaptor.capture());
        List<PlanBenefitGroup> savedList = savedPbgCaptor.getValue();
        assertEquals(1, savedList.size());
        assertTrue(savedList.stream().anyMatch(pbg ->
                pbg.getBenefitGroup().getBenefitGroupId().equals(mockNewBenefitGroup.getBenefitGroupId())
        ));


        verify(planRepository, times(1)).save(any(Plan.class));
    }

    @Test
    @DisplayName("존재하지 않는 ID 업데이트 시 예외 발생")
    void updatePlan_InvalidId_ThrowsPlanNotFoundException() {
        // Given
        Integer invalidPlanId = 999;
        when(planRepository.findById((int) invalidPlanId.longValue())).thenReturn(Optional.empty());

        // When & Then
        PlanDto mockPlanDto = null;
        PlanException exception = assertThrows(PlanException.class, () -> planService.updatePlan(invalidPlanId, mockPlanDto));
        assertEquals(ErrorCode.PLAN_NOT_FOUND, exception.getErrorCode());
        verify(planRepository, times(1)).findById((int) invalidPlanId.longValue());
        verify(planRepository, never()).findByPlanName(anyString());
    }

    @Test
    @DisplayName("중복 이름으로 업데이트 시 예외 발생")
    void updatePlan_DuplicateName_ThrowsPlanAlreadyExistsException() {
        // Given
        Integer planIdAsInteger = mockPlan.getPlanId().intValue();
        PlanDto updatedPlanDto = PlanDto.builder().planName("Another Existing Plan").build();
        Plan anotherPlan = Plan.builder().planId(99L).planName("Another Existing Plan").build();

        when(planRepository.findById(Math.toIntExact(mockPlan.getPlanId()))).thenReturn(Optional.of(mockPlan));
        when(planRepository.findByPlanName(updatedPlanDto.getPlanName())).thenReturn(Optional.of(anotherPlan));

        // When & Then
        PlanException exception = assertThrows(PlanException.class, () -> planService.updatePlan(planIdAsInteger, updatedPlanDto));
        assertEquals(ErrorCode.PLAN_ALREADY_EXISTS, exception.getErrorCode());
        verify(planRepository, times(1)).findById(Math.toIntExact(mockPlan.getPlanId()));
        verify(planRepository, times(1)).findByPlanName(updatedPlanDto.getPlanName());
        verify(duplicateChecker, never()).findOrCreatePlanCategory(anyString());
    }

    @Test
    @DisplayName("업데이트 중 오류 발생 시 예외 발생")
    void updatePlan_UnexpectedError_ThrowsPlanUpdateFailureException() {
        // Given
        Integer planIdAsInteger = mockPlan.getPlanId().intValue();
        PlanDto updatedPlanDto = PlanDto.builder().planName("Update Error Plan").build();
        when(planRepository.findById(Math.toIntExact(mockPlan.getPlanId()))).thenReturn(Optional.of(mockPlan));
        when(planRepository.findByPlanName(updatedPlanDto.getPlanName())).thenReturn(Optional.empty());
        when(duplicateChecker.findOrCreatePlanCategory(anyString())).thenThrow(new RuntimeException("DB Error during update"));

        // When & Then
        PlanException exception = assertThrows(PlanException.class, () -> planService.updatePlan(planIdAsInteger, updatedPlanDto));
        assertEquals(ErrorCode.PLAN_UPDATE_FAILURE, exception.getErrorCode());
        verify(planRepository, times(1)).findById(Math.toIntExact(mockPlan.getPlanId()));
        verify(planRepository, times(1)).findByPlanName(updatedPlanDto.getPlanName());
        verify(planBenefitGroupRepository, never()).deleteAll(anyList());
    }


    // --- getAllBenefitsByPlanId 테스트 ---
    @Test
    @DisplayName("Plan ID로 모든 혜택 조회")
    void getAllBenefitsByPlanId_ValidPlanId_ReturnsListOfBenefitDtos() {
        // Given
        Integer planIdAsInteger = mockPlan.getPlanId().intValue();

        when(planRepository.existsById(planIdAsInteger)).thenReturn(true);
        when(planBenefitGroupRepository.findAllWithBenefitsByPlanId(planIdAsInteger))
                .thenReturn(Arrays.asList(mockPlanBenefitGroup1, mockPlanBenefitGroup2));

        // When
        List<BenefitDto> result = planService.getAllBenefitsByPlanId(planIdAsInteger);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(b -> b.getBenefitId().equals(mockBenefit1.getBenefitId())));
        assertTrue(result.stream().anyMatch(b -> b.getBenefitId().equals(mockBenefit2.getBenefitId())));
        verify(planRepository, times(1)).existsById(planIdAsInteger);
        verify(planBenefitGroupRepository, times(1)).findAllWithBenefitsByPlanId(planIdAsInteger);
    }

    @Test
    @DisplayName("유효하지 않은 Plan ID로 혜택 조회 시 예외 발생")
    void getAllBenefitsByPlanId_InvalidPlanId_ThrowsPlanNotFoundException() {
        // Given
        Integer invalidPlanId = 999;
        when(planRepository.existsById(invalidPlanId)).thenReturn(false);

        // When & Then
        PlanException exception = assertThrows(PlanException.class, () -> planService.getAllBenefitsByPlanId(invalidPlanId));
        assertEquals(ErrorCode.PLAN_NOT_FOUND, exception.getErrorCode());
        verify(planRepository, times(1)).existsById(invalidPlanId);
        verify(planBenefitGroupRepository, never()).findAllWithBenefitsByPlanId(anyInt());
    }

    @Test
    @DisplayName("혜택 없는 요금제 혜택 조회 시 빈 목록 반환")
    void getAllBenefitsByPlanId_NoBenefits_ReturnsEmptyList() {
        // Given
        Integer planId = mockPlan.getPlanId().intValue();
        when(planRepository.existsById(planId)).thenReturn(true);
        when(planBenefitGroupRepository.findAllWithBenefitsByPlanId(planId)).thenReturn(Collections.emptyList());

        // When
        List<BenefitDto> result = planService.getAllBenefitsByPlanId(planId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(planRepository, times(1)).existsById(planId);
        verify(planBenefitGroupRepository, times(1)).findAllWithBenefitsByPlanId(planId);
    }


    // --- getPlansByPlanBenefitIds 테스트 ---
    @Test
    @DisplayName("PlanBenefit ID 목록으로 요금제 조회")
    void getPlansByPlanBenefitIds_ValidIds_ReturnsListOfPlanBenefitDtos() {
        // Given
        List<Long> planBenefitIds = Arrays.asList(1L, 2L);
        Plan mockPlan2 = Plan.builder()
                .planId(2L)
                .planName("Another Plan")
                .monthlyFee(40000)
                .planCategory(mockCategory)
                .dataAllowances(mockDataAllowances)
                .sharedData(mockSharedData)
                .voiceCall(mockVoiceCall)
                .planBenefitGroups(new HashSet<>())
                .build();
        PlanBenefitGroup pbg1 = PlanBenefitGroup.builder().planBenefitId(1L).plan(mockPlan).benefitGroup(mockBenefitGroup1).build();
        PlanBenefitGroup pbg2 = PlanBenefitGroup.builder().planBenefitId(2L).plan(mockPlan2).benefitGroup(mockBenefitGroup2).build();

        when(planBenefitGroupRepository.findAllByIdWithPlan(planBenefitIds)).thenReturn(Arrays.asList(pbg1, pbg2));

        // When
        List<PlanBenefitDto> result = planService.getPlansByPlanBenefitIds(planBenefitIds);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(mockPlan.getPlanId().intValue(), result.get(0).getPlanId());
        assertEquals(mockPlan2.getPlanId().intValue(), result.get(1).getPlanId());
        verify(planBenefitGroupRepository, times(1)).findAllByIdWithPlan(planBenefitIds);
    }

    @Test
    @DisplayName("빈 PlanBenefit ID 목록 시 빈 목록 반환")
    void getPlansByPlanBenefitIds_EmptyIds_ReturnsEmptyList() {
        // Given
        List<Long> emptyList = Collections.emptyList();

        // When
        List<PlanBenefitDto> result = planService.getPlansByPlanBenefitIds(emptyList);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(planBenefitGroupRepository, never()).findAllByIdWithPlan(anyList());
    }

    @Test
    @DisplayName("Null PlanBenefit ID 목록 시 빈 목록 반환")
    void getPlansByPlanBenefitIds_NullIds_ReturnsEmptyList() {
        // Given
        List<Long> nullList = null;

        // When
        List<PlanBenefitDto> result = planService.getPlansByPlanBenefitIds(nullList);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(planBenefitGroupRepository, never()).findAllByIdWithPlan(anyList());
    }


    // --- checkPlanHasBenefitGroup 테스트 ---
    @Test
    @DisplayName("요금제가 혜택 그룹 포함 시 true")
    void checkPlanHasBenefitGroup_Exists_ReturnsTrue() {
        // Given
        Integer planId = mockPlan.getPlanId().intValue();
        Long benefitGroupId = mockBenefitGroup1.getBenefitGroupId();
        when(planBenefitGroupRepository.existsByPlan_PlanIdAndBenefitGroup_BenefitGroupId(planId, benefitGroupId)).thenReturn(true);

        // When
        boolean result = planService.checkPlanHasBenefitGroup(planId, benefitGroupId);

        // Then
        assertTrue(result);
        verify(planBenefitGroupRepository, times(1)).existsByPlan_PlanIdAndBenefitGroup_BenefitGroupId(planId, benefitGroupId);
    }

    @Test
    @DisplayName("요금제가 혜택 그룹 미포함 시 false")
    void checkPlanHasBenefitGroup_NotExists_ReturnsFalse() {
        // Given
        Integer planId = mockPlan.getPlanId().intValue();
        Long benefitGroupId = 99L;
        when(planBenefitGroupRepository.existsByPlan_PlanIdAndBenefitGroup_BenefitGroupId(planId, benefitGroupId)).thenReturn(false);

        // When
        boolean result = planService.checkPlanHasBenefitGroup(planId, benefitGroupId);

        // Then
        assertFalse(result);
        verify(planBenefitGroupRepository, times(1)).existsByPlan_PlanIdAndBenefitGroup_BenefitGroupId(planId, benefitGroupId);
    }

    // --- findPlansByPlanNameContaining 테스트 ---
    @Test
    @DisplayName("검색어로 요금제 조회")
    void findPlansByPlanNameContaining_WithSearchTerm_ReturnsFilteredList() {
        // Given
        String searchTerm = "Test";
        Plan anotherPlan = Plan.builder().planId(2L).planName("Another Test Plan").monthlyFee(40000)
                .planCategory(mockCategory)
                .dataAllowances(mockDataAllowances)
                .sharedData(mockSharedData)
                .voiceCall(mockVoiceCall)
                .planBenefitGroups(new HashSet<>())
                .build();

        PlanBenefitGroup pbgForAnother = PlanBenefitGroup.builder().planBenefitId(3L).plan(anotherPlan).benefitGroup(mockBenefitGroup1).build();
        anotherPlan.getPlanBenefitGroups().add(pbgForAnother);

        when(planRepository.findByPlanNameContainingIgnoreCase(searchTerm)).thenReturn(Arrays.asList(mockPlan, anotherPlan));

        // When
        List<PlanDto> result = planService.findPlansByPlanNameContaining(searchTerm);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(p -> p.getPlanName().equals("Test Plan")));
        assertTrue(result.stream().anyMatch(p -> p.getPlanName().equals("Another Test Plan")));
        verify(planRepository, times(1)).findByPlanNameContainingIgnoreCase(searchTerm);
        verify(planRepository, never()).findAllWithBenefits();
    }

    @Test
    @DisplayName("검색어 비어있을 때 전체 요금제 반환")
    void findPlansByPlanNameContaining_NullOrEmptySearchTerm_ReturnsAllPlans() {
        // Given
        String nullSearchTerm = null;
        String emptySearchTerm = "";
        String blankSearchTerm = "   ";

        when(planRepository.findAllWithBenefits()).thenReturn(Collections.singletonList(mockPlan));

        // When
        List<PlanDto> resultNull = planService.findPlansByPlanNameContaining(nullSearchTerm);
        List<PlanDto> resultEmpty = planService.findPlansByPlanNameContaining(emptySearchTerm);
        List<PlanDto> resultBlank = planService.findPlansByPlanNameContaining(blankSearchTerm);


        // Then
        assertNotNull(resultNull);
        assertFalse(resultNull.isEmpty());
        assertEquals(1, resultNull.size());
        assertEquals("Test Plan", resultNull.get(0).getPlanName());
        verify(planRepository, times(3)).findAllWithBenefits();
        verify(planRepository, never()).findByPlanNameContainingIgnoreCase(anyString());
    }


    // --- getFilteredPlans 테스트 ---
    @Test
    @DisplayName("필터링된 요금제 응답 DTO 반환")
    void getFilteredPlans_ReturnsFilteredResponseDtos() {
        // Given
        PlanFilterRequestDto filterRequest = PlanFilterRequestDto.builder()
                .categoryIds(Arrays.asList(1L))
                .allCategoriesSelected(false)
                .priceRanges(Arrays.asList("30000-50000"))
                .anyPriceSelected(false)
                .dataOptions(Arrays.asList("10GB"))
                .anyDataSelected(false)
                .benefitIds(Arrays.asList(mockBenefit1.getBenefitId()))
                .noBenefitsSelected(false)
                .build();

        when(planRepository.findPlansWithFilter(filterRequest)).thenReturn(Collections.singletonList(mockPlan));

        // When
        List<PlanFilterResponseDto> result = planService.getFilteredPlans(filterRequest);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        PlanFilterResponseDto dto = result.get(0);
        assertEquals(mockPlan.getPlanId().intValue(), dto.getPlanId());
        assertEquals(mockPlan.getPlanName(), dto.getPlanName());
        assertEquals(mockPlan.getMonthlyFee(), dto.getMonthlyFee());
        assertNotNull(dto.getDataAllowance());
        assertEquals(mockDataAllowances.getDataAmount(), dto.getDataAllowance().getDataAmount());
        assertNotNull(dto.getBenefits());
        assertEquals(2, dto.getBenefits().size());
        assertTrue(dto.getBenefits().stream().anyMatch(b -> b.getBenefitId().equals(mockBenefit1.getBenefitId())));
        verify(planRepository, times(1)).findPlansWithFilter(filterRequest);
    }

    @Test
    @DisplayName("필터링된 요금제 없을 때 빈 목록 반환")
    void getFilteredPlans_NoFilteredPlans_ReturnsEmptyList() {
        // Given
        PlanFilterRequestDto filterRequest = PlanFilterRequestDto.builder()
                .categoryIds(Arrays.asList(99L))
                .allCategoriesSelected(false)
                .build();

        when(planRepository.findPlansWithFilter(filterRequest)).thenReturn(Collections.emptyList());

        // When
        List<PlanFilterResponseDto> result = planService.getFilteredPlans(filterRequest);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(planRepository, times(1)).findPlansWithFilter(filterRequest);
    }


    // --- countPlansWithFilter 테스트 ---
    @Test
    @DisplayName("필터링된 요금제 개수 반환")
    void countPlansWithFilter_ReturnsCorrectCount() {
        // Given
        PlanFilterRequestDto requestDto = PlanFilterRequestDto.builder()
                .categoryIds(Arrays.asList(1L))
                .allCategoriesSelected(false)
                .priceRanges(Arrays.asList("30000-50000"))
                .anyPriceSelected(false)
                .build();
        when(planRepository.countPlansWithFilter(requestDto)).thenReturn(5);

        // When
        int count = planService.countPlansWithFilter(requestDto);

        // Then
        assertEquals(5, count);
        verify(planRepository, times(1)).countPlansWithFilter(requestDto);
    }


    // --- getFilteredList 테스트 ---
    @Test
    @DisplayName("필터링된 목록 응답 DTO 반환")
    void getFilteredList_NoFilteredList_ReturnsEmptyList() {
        // Given
        PlanFilterRequestDto filterRequest = PlanFilterRequestDto.builder()
                .categoryIds(Arrays.asList(99L))
                .build();
        when(planRepository.getFilteredList(filterRequest)).thenReturn(Collections.emptyList());

        // When
        List<FilterListResponseDto> result = planService.getFilteredList(filterRequest);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(planRepository, times(1)).getFilteredList(filterRequest);
    }

    @Test
    @DisplayName("필터링된 목록 없을 때 빈 목록 반환")
    void getFilteredList_ReturnsFilteredListResponseDtos() {
        // Given
        PlanFilterRequestDto filterRequest = PlanFilterRequestDto.builder()
                .priceRanges(Arrays.asList("30000-50000", "50000-70000"))
                .anyPriceSelected(false)
                .build();

        FilterListResponseDto expectedDto = FilterListResponseDto.builder()
                .planId(mockPlan.getPlanId().intValue())
                .planName(mockPlan.getPlanName())
                .planCategory(mockCategory.getCategoryName())
                .dataAllowance(mockDataAllowances.getDataAmount())
                .dataAllowanceUnit(mockDataAllowances.getDataUnit())
                .dataPeriod(mockDataAllowances.getDataPeriod())
                .tetheringDataAmount(mockSharedData.getTetheringDataAmount())
                .tetheringDataUnit(mockSharedData.getTetheringDataUnit())
                .familyDataAmount(mockSharedData.getFamilyDataAmount())
                .familyDataUnit(mockSharedData.getFamilyDataUnit())
                .voiceAllowance(mockVoiceCall.getVoiceAllowance())
                .additionalCallAllowance(mockVoiceCall.getAdditionalCallAllowance())
                .monthlyFee(mockPlan.getMonthlyFee())
                .benefitIdList(Arrays.asList(mockBenefit1.getBenefitId(), mockBenefit2.getBenefitId()))
                .build();

        when(planRepository.getFilteredList(filterRequest)).thenReturn(Collections.singletonList(expectedDto));

        // When
        List<FilterListResponseDto> result = planService.getFilteredList(filterRequest);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(expectedDto.getPlanName(), result.get(0).getPlanName());
        assertEquals(expectedDto.getPlanId(), result.get(0).getPlanId());
        assertEquals(expectedDto.getMonthlyFee(), result.get(0).getMonthlyFee());
        assertEquals(expectedDto.getDataAllowance(), result.get(0).getDataAllowance());
        assertEquals(expectedDto.getBenefitIdList(), result.get(0).getBenefitIdList());

        verify(planRepository, times(1)).getFilteredList(filterRequest);
    }


    // --- getBenefitGroupsByPlanIds 테스트 ---
    @Test
    @DisplayName("혜택 ID로 혜택 그룹 ID 반환")
    void getBenefitGroupsByPlanIds_ReturnsBenefitGroupId() {
        // Given
        List<Long> benefitIds = Arrays.asList(mockBenefit1.getBenefitId(), mockBenefit2.getBenefitId());
        when(planRepository.findBenefitGroupIdsByAllBenefitIds(benefitIds)).thenReturn(Optional.of(mockBenefitGroup1));

        // When
        Long result = planService.getBenefitGroupsByPlanIds(benefitIds);

        // Then
        assertNotNull(result);
        assertEquals(mockBenefitGroup1.getBenefitGroupId(), result);
        verify(planRepository, times(1)).findBenefitGroupIdsByAllBenefitIds(benefitIds);
    }

    @Test
    @DisplayName("일치하는 혜택 그룹 없을 때 Null 반환")
    void getBenefitGroupsByPlanIds_NoMatchingGroup_ReturnsNull() {
        // Given
        List<Long> benefitIds = Arrays.asList(999L, 888L);
        when(planRepository.findBenefitGroupIdsByAllBenefitIds(benefitIds)).thenReturn(Optional.empty());

        // When
        Long result = planService.getBenefitGroupsByPlanIds(benefitIds);

        // Then
        assertNull(result);
        verify(planRepository, times(1)).findBenefitGroupIdsByAllBenefitIds(benefitIds);
    }


    // --- getPlanBenefitGroupId 테스트 ---
    @Test
    @DisplayName("Plan ID와 혜택 그룹 ID로 PlanBenefit ID 반환")
    void getPlanBenefitGroupId_ReturnsPlanBenefitId() {
        // Given
        GetPlanBenefitGroupIdRequestDto requestDto = GetPlanBenefitGroupIdRequestDto.builder()
                .planId(mockPlan.getPlanId().intValue())
                .benefitGroupId(mockBenefitGroup1.getBenefitGroupId())
                .build();
        PlanBenefitGroup expectedPbg = PlanBenefitGroup.builder()
                .planBenefitId(1L)
                .plan(mockPlan)
                .benefitGroup(mockBenefitGroup1)
                .build();

        when(planRepository.getPlanBenefitGroupId(requestDto)).thenReturn(expectedPbg);

        // When
        Long result = planService.getPlanBenefitGroupId(requestDto);

        // Then
        assertNotNull(result);
        assertEquals(expectedPbg.getPlanBenefitId(), result);
        verify(planRepository, times(1)).getPlanBenefitGroupId(requestDto);
    }
}
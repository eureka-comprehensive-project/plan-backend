package com.comprehensive.eureka.plan.service.impl;

import com.comprehensive.eureka.plan.dto.BenefitDto;
import com.comprehensive.eureka.plan.dto.request.BenefitRequestDto;
import com.comprehensive.eureka.plan.entity.Benefit;
import com.comprehensive.eureka.plan.entity.BenefitGroup;
import com.comprehensive.eureka.plan.entity.BenefitGroupBenefit;
import com.comprehensive.eureka.plan.entity.PlanBenefitGroup;
import com.comprehensive.eureka.plan.entity.enums.BenefitType;
import com.comprehensive.eureka.plan.exception.ErrorCode;
import com.comprehensive.eureka.plan.exception.PlanException;
import com.comprehensive.eureka.plan.repository.BenefitGroupRepository;
import com.comprehensive.eureka.plan.repository.BenefitRepository;
import com.comprehensive.eureka.plan.repository.PlanBenefitGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("혜택 서비스 단위 테스트")
class BenefitServiceImplTest {

    @Mock
    private BenefitRepository benefitRepository;
    @Mock
    private BenefitGroupRepository benefitGroupRepository;
    @Mock
    private PlanBenefitGroupRepository planBenefitGroupRepository;

    @InjectMocks
    private BenefitServiceImpl benefitService;

    private Benefit mockPremiumBenefit;
    private Benefit mockMediaBenefit;
    private Benefit mockBasicBenefit;
    private Benefit mockAnotherMediaBenefit;

    private BenefitGroup mockBenefitGroupWithPremium;
    private BenefitGroup mockBenefitGroupWithMedia;
    private BenefitGroup mockBenefitGroupWithPremiumAndMedia;
    private BenefitGroup mockBenefitGroupWithMediaOnly;
    private BenefitGroup mockBenefitGroupWithMediaOnly2;

    private PlanBenefitGroup mockPlanBenefitGroup;

    @BeforeEach
    void setUp() {
        mockPremiumBenefit = Benefit.builder()
                .benefitId(1L)
                .benefitName("Netflix Premium")
                .benefitType(BenefitType.PREMIUM)
                .benefitGroupBenefits(new ArrayList<>())
                .build();
        mockMediaBenefit = Benefit.builder()
                .benefitId(2L)
                .benefitName("Youtube Premium")
                .benefitType(BenefitType.MEDIA)
                .benefitGroupBenefits(new ArrayList<>())
                .build();
        mockBasicBenefit = Benefit.builder()
                .benefitId(3L)
                .benefitName("Basic Data")
                .benefitType(BenefitType.BASIC)
                .benefitGroupBenefits(new ArrayList<>())
                .build();
        mockAnotherMediaBenefit = Benefit.builder()
                .benefitId(4L)
                .benefitName("Spotify Premium")
                .benefitType(BenefitType.MEDIA)
                .benefitGroupBenefits(new ArrayList<>())
                .build();

        mockBenefitGroupWithMedia = BenefitGroup.builder()
                .benefitGroupId(10L)
                .description("Media Only Group (Youtube)")
                .benefitGroupBenefits(new HashSet<>())
                .planBenefitGroups(new ArrayList<>())
                .build();

        mockBenefitGroupWithPremiumAndMedia = BenefitGroup.builder()
                .benefitGroupId(20L)
                .description("Premium and Media Group (Netflix, Youtube)")
                .benefitGroupBenefits(new HashSet<>())
                .planBenefitGroups(new ArrayList<>())
                .build();

        mockBenefitGroupWithMediaOnly = BenefitGroup.builder()
                .benefitGroupId(30L)
                .description("Media Only Group (Spotify)")
                .benefitGroupBenefits(new HashSet<>())
                .planBenefitGroups(new ArrayList<>())
                .build();

        mockBenefitGroupWithMediaOnly2 = BenefitGroup.builder()
                .benefitGroupId(31L)
                .description("Media Only Group 2 (Spotify)")
                .benefitGroupBenefits(new HashSet<>())
                .planBenefitGroups(new ArrayList<>())
                .build();

        BenefitGroupBenefit bgb_media = BenefitGroupBenefit.builder()
                .benefitGroupBenefitId(101L)
                .benefit(mockMediaBenefit)
                .benefitGroup(mockBenefitGroupWithMedia)
                .build();
        mockBenefitGroupWithMedia.getBenefitGroupBenefits().add(bgb_media);
        mockMediaBenefit.getBenefitGroupBenefits().add(bgb_media);

        BenefitGroupBenefit bgb_premium_and_media_1 = BenefitGroupBenefit.builder()
                .benefitGroupBenefitId(102L)
                .benefit(mockPremiumBenefit)
                .benefitGroup(mockBenefitGroupWithPremiumAndMedia)
                .build();
        BenefitGroupBenefit bgb_premium_and_media_2 = BenefitGroupBenefit.builder()
                .benefitGroupBenefitId(103L)
                .benefit(mockMediaBenefit)
                .benefitGroup(mockBenefitGroupWithPremiumAndMedia)
                .build();
        mockBenefitGroupWithPremiumAndMedia.getBenefitGroupBenefits().add(bgb_premium_and_media_1);
        mockBenefitGroupWithPremiumAndMedia.getBenefitGroupBenefits().add(bgb_premium_and_media_2);
        mockPremiumBenefit.getBenefitGroupBenefits().add(bgb_premium_and_media_1);
        mockMediaBenefit.getBenefitGroupBenefits().add(bgb_premium_and_media_2);

        BenefitGroupBenefit bgb_another_media = BenefitGroupBenefit.builder()
                .benefitGroupBenefitId(104L)
                .benefit(mockAnotherMediaBenefit)
                .benefitGroup(mockBenefitGroupWithMediaOnly)
                .build();
        mockBenefitGroupWithMediaOnly.getBenefitGroupBenefits().add(bgb_another_media);
        mockAnotherMediaBenefit.getBenefitGroupBenefits().add(bgb_another_media);

        BenefitGroupBenefit bgb_another_media_2 = BenefitGroupBenefit.builder()
                .benefitGroupBenefitId(105L)
                .benefit(mockAnotherMediaBenefit)
                .benefitGroup(mockBenefitGroupWithMediaOnly2)
                .build();
        mockBenefitGroupWithMediaOnly2.getBenefitGroupBenefits().add(bgb_another_media_2);
        mockAnotherMediaBenefit.getBenefitGroupBenefits().add(bgb_another_media_2);


        // PlanBenefitGroup 초기화
        mockPlanBenefitGroup = PlanBenefitGroup.builder()
                .planBenefitId(1L)
                .plan(null)
                .benefitGroup(mockBenefitGroupWithPremiumAndMedia)
                .build();
    }

    // --- getAllBenefitsByType 테스트 ---
    @Test
    @DisplayName("유효 타입으로 혜택 조회")
    void getAllBenefitsByType_ValidType_ReturnsBenefitDtoList() {
        // Given
        when(benefitRepository.findAllByBenefitType(BenefitType.MEDIA)).thenReturn(Arrays.asList(mockMediaBenefit, mockAnotherMediaBenefit));

        // When
        List<BenefitDto> result = benefitService.getAllBenefitsByType("MEDIA");

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(b -> b.getBenefitId().equals(mockMediaBenefit.getBenefitId()) && b.getBenefitName().equals("Youtube Premium")));
        assertTrue(result.stream().anyMatch(b -> b.getBenefitId().equals(mockAnotherMediaBenefit.getBenefitId()) && b.getBenefitName().equals("Spotify Premium")));
        verify(benefitRepository, times(1)).findAllByBenefitType(BenefitType.MEDIA);
    }

    @Test
    @DisplayName("유효하지 않은 타입 조회 시 예외")
    void getAllBenefitsByType_InvalidType_ThrowsPlanException() {
        // Given
        String invalidType = "INVALID_TYPE";

        // When & Then
        PlanException exception = assertThrows(PlanException.class, () -> benefitService.getAllBenefitsByType(invalidType));
        assertEquals(ErrorCode.BENEFIT_NOT_FOUND, exception.getErrorCode());

        verify(benefitRepository, never()).findAllByBenefitType(any(BenefitType.class));
    }

    @Test
    @DisplayName("혜택 없을 때 빈 목록 반환")
    void getAllBenefitsByType_NoBenefitsFound_ReturnsEmptyList() {
        // Given
        when(benefitRepository.findAllByBenefitType(BenefitType.BASIC)).thenReturn(Collections.emptyList());

        // When
        List<BenefitDto> result = benefitService.getAllBenefitsByType("BASIC");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(benefitRepository, times(1)).findAllByBenefitType(BenefitType.BASIC);
    }

    // --- getAllBenefits 테스트 ---
    @Test
    @DisplayName("모든 혜택 조회")
    void getAllBenefits_ReturnsAllBenefitDtos() {
        // Given
        when(benefitRepository.findAll()).thenReturn(Arrays.asList(mockPremiumBenefit, mockMediaBenefit, mockBasicBenefit));

        // When
        List<BenefitDto> result = benefitService.getAllBenefits();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(b -> b.getBenefitId().equals(mockPremiumBenefit.getBenefitId())));
        assertTrue(result.stream().anyMatch(b -> b.getBenefitId().equals(mockMediaBenefit.getBenefitId())));
        assertTrue(result.stream().anyMatch(b -> b.getBenefitId().equals(mockBasicBenefit.getBenefitId())));
        verify(benefitRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("혜택 없을 때 빈 목록 반환")
    void getAllBenefits_NoBenefitsFound_ReturnsEmptyList() {
        // Given
        when(benefitRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<BenefitDto> result = benefitService.getAllBenefits();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(benefitRepository, times(1)).findAll();
    }


    // --- findBenefitGroupIdByBenefits 테스트 ---
    @Test
    @DisplayName("정확히 일치하는 혜택 그룹 ID 반환")
    void findBenefitGroupIdByBenefits_ExactMatch_ReturnsBenefitGroupId() {
        // Given
        BenefitRequestDto requestDto = BenefitRequestDto.builder()
                .premium(mockPremiumBenefit.getBenefitName())
                .media(mockMediaBenefit.getBenefitName())
                .build();
        List<String> requestedBenefitNames = Arrays.asList(mockPremiumBenefit.getBenefitName(), mockMediaBenefit.getBenefitName());
        List<Long> requestedBenefitIds = Arrays.asList(mockPremiumBenefit.getBenefitId(), mockMediaBenefit.getBenefitId());

        when(benefitRepository.findAllByBenefitNameIn(requestedBenefitNames)).thenReturn(Arrays.asList(mockPremiumBenefit, mockMediaBenefit));

        when(benefitGroupRepository.findBenefitGroupsByExactBenefits(argThat(ids ->
                new HashSet<>(ids).equals(new HashSet<>(requestedBenefitIds)) && ids.size() == requestedBenefitIds.size()
        ))).thenReturn(Collections.singletonList(mockBenefitGroupWithPremiumAndMedia));

        // When
        Long result = benefitService.findBenefitGroupIdByBenefits(requestDto);

        // Then
        assertNotNull(result);
        assertEquals(mockBenefitGroupWithPremiumAndMedia.getBenefitGroupId(), result);

        verify(benefitRepository, times(1)).findAllByBenefitNameIn(requestedBenefitNames);
        verify(benefitGroupRepository, times(1)).findBenefitGroupsByExactBenefits(anyList());
    }

    @Test
    @DisplayName("혜택 이름 비어있을 때 0L 반환")
    void findBenefitGroupIdByBenefits_EmptyNames_ReturnsZero() {
        // Given
        BenefitRequestDto requestDto = BenefitRequestDto.builder()
                .premium("")
                .media("   ")
                .build();

        // When
        Long result = benefitService.findBenefitGroupIdByBenefits(requestDto);

        // Then
        assertEquals(0L, result);
        verify(benefitRepository, never()).findAllByBenefitNameIn(anyList());
    }

    @Test
    @DisplayName("일부 혜택 없을 때 0L 반환")
    void findBenefitGroupIdByBenefits_SomeBenefitsNotFound_ReturnsZero() {
        // Given
        BenefitRequestDto requestDto = BenefitRequestDto.builder()
                .premium("NonExistent Premium")
                .media(mockMediaBenefit.getBenefitName())
                .build();
        List<String> requestedBenefitNames = Arrays.asList("NonExistent Premium", mockMediaBenefit.getBenefitName());

        when(benefitRepository.findAllByBenefitNameIn(requestedBenefitNames)).thenReturn(Collections.singletonList(mockMediaBenefit));

        // When
        Long result = benefitService.findBenefitGroupIdByBenefits(requestDto);

        // Then
        assertEquals(0L, result);
        verify(benefitRepository, times(1)).findAllByBenefitNameIn(requestedBenefitNames);
        verify(benefitGroupRepository, never()).findBenefitGroupsByExactBenefits(anyList());
    }

    @Test
    @DisplayName("일치하는 그룹 없을 때 0L 반환")
    void findBenefitGroupIdByBenefits_NoMatchingGroup_ReturnsZero() {
        // Given
        BenefitRequestDto requestDto = BenefitRequestDto.builder()
                .premium(mockPremiumBenefit.getBenefitName())
                .media(mockMediaBenefit.getBenefitName())
                .build();
        List<String> requestedBenefitNames = Arrays.asList(mockPremiumBenefit.getBenefitName(), mockMediaBenefit.getBenefitName());
        List<Long> requestedBenefitIds = Arrays.asList(mockPremiumBenefit.getBenefitId(), mockMediaBenefit.getBenefitId());

        when(benefitRepository.findAllByBenefitNameIn(requestedBenefitNames)).thenReturn(Arrays.asList(mockPremiumBenefit, mockMediaBenefit));
        when(benefitGroupRepository.findBenefitGroupsByExactBenefits(anyList())).thenReturn(Collections.emptyList());

        // When
        Long result = benefitService.findBenefitGroupIdByBenefits(requestDto);

        // Then
        assertEquals(0L, result);
        verify(benefitRepository, times(1)).findAllByBenefitNameIn(requestedBenefitNames);
        verify(benefitGroupRepository, times(1)).findBenefitGroupsByExactBenefits(anyList());
    }

    @Test
    @DisplayName("중복 그룹 발견 시 예외")
    void findBenefitGroupIdByBenefits_MultipleMatchingGroups_ThrowsPlanException() {
        // Given
        BenefitRequestDto requestDto = BenefitRequestDto.builder()
                .media(mockAnotherMediaBenefit.getBenefitName())
                .build();
        List<String> requestedBenefitNames = Collections.singletonList(mockAnotherMediaBenefit.getBenefitName());
        List<Long> requestedBenefitIds = Collections.singletonList(mockAnotherMediaBenefit.getBenefitId());

        when(benefitRepository.findAllByBenefitNameIn(requestedBenefitNames)).thenReturn(Collections.singletonList(mockAnotherMediaBenefit));
        when(benefitGroupRepository.findBenefitGroupsByExactBenefits(anyList()))
                .thenReturn(Arrays.asList(mockBenefitGroupWithMediaOnly, mockBenefitGroupWithMediaOnly2));

        // When & Then
        PlanException exception = assertThrows(PlanException.class, () -> benefitService.findBenefitGroupIdByBenefits(requestDto));
        assertEquals(ErrorCode.BENEFIT_GROUP_NOT_FOUND, exception.getErrorCode());

        verify(benefitRepository, times(1)).findAllByBenefitNameIn(requestedBenefitNames);
        verify(benefitGroupRepository, times(1)).findBenefitGroupsByExactBenefits(anyList());
    }


    // --- getBenefitsByPlanBenefitGroupId 테스트 ---
    @Test
    @DisplayName("유효 ID로 혜택 조회 (Set)")
    void getBenefitsByPlanBenefitGroupId_ValidId_ReturnsBenefitDtoSet() {
        // Given
        Long pbgId = mockPlanBenefitGroup.getPlanBenefitId();
        when(planBenefitGroupRepository.findPlanBenefitGroupWithBenefits(pbgId)).thenReturn(Optional.of(mockPlanBenefitGroup));

        // When
        Set<BenefitDto> result = benefitService.getBenefitsByPlanBenefitGroupId(pbgId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // mockPremiumBenefit, mockMediaBenefit 포함
        assertTrue(result.stream().anyMatch(b -> b.getBenefitId().equals(mockPremiumBenefit.getBenefitId())));
        assertTrue(result.stream().anyMatch(b -> b.getBenefitId().equals(mockMediaBenefit.getBenefitId())));
        verify(planBenefitGroupRepository, times(1)).findPlanBenefitGroupWithBenefits(pbgId);
    }

    @Test
    @DisplayName("유효하지 않은 ID 조회 시 예외")
    void getBenefitsByPlanBenefitGroupId_InvalidId_ThrowsIllegalArgumentException() {
        // Given
        Long invalidPbgId = 999L;
        when(planBenefitGroupRepository.findPlanBenefitGroupWithBenefits(invalidPbgId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> benefitService.getBenefitsByPlanBenefitGroupId(invalidPbgId));
        assertTrue(exception.getMessage().contains("요금제_혜택모음ID [" + invalidPbgId + "]에 해당하는 혜택 모음을 찾을 수 없습니다."));
        verify(planBenefitGroupRepository, times(1)).findPlanBenefitGroupWithBenefits(invalidPbgId);
    }

    @Test
    @DisplayName("혜택 그룹 Null 시 빈 Set")
    void getBenefitsByPlanBenefitGroupId_BenefitGroupNull_ReturnsEmptySet() {
        // Given
        Long pbgId = 100L;

        PlanBenefitGroup pbgWithNullBenefitGroup = PlanBenefitGroup.builder()
                .planBenefitId(pbgId)
                .plan(null)
                .benefitGroup(null)
                .build();
        when(planBenefitGroupRepository.findPlanBenefitGroupWithBenefits(pbgId)).thenReturn(Optional.of(pbgWithNullBenefitGroup));

        // When
        Set<BenefitDto> result = benefitService.getBenefitsByPlanBenefitGroupId(pbgId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(planBenefitGroupRepository, times(1)).findPlanBenefitGroupWithBenefits(pbgId);
    }

    @Test
    @DisplayName("혜택 그룹 혜택 Null 시 빈 Set")
    void getBenefitsByPlanBenefitGroupId_BenefitGroupBenefitsNull_ReturnsEmptySet() {
        // Given
        Long pbgId = 101L;

        BenefitGroup benefitGroupWithNullBenefits = BenefitGroup.builder()
                .benefitGroupId(40L)
                .description("Null Benefits Group")
                .benefitGroupBenefits(null)
                .planBenefitGroups(new ArrayList<>())
                .build();
        PlanBenefitGroup pbgWithNullBenefitGroupBenefits = PlanBenefitGroup.builder()
                .planBenefitId(pbgId)
                .plan(null)
                .benefitGroup(benefitGroupWithNullBenefits)
                .build();
        when(planBenefitGroupRepository.findPlanBenefitGroupWithBenefits(pbgId)).thenReturn(Optional.of(pbgWithNullBenefitGroupBenefits));

        // When
        Set<BenefitDto> result = benefitService.getBenefitsByPlanBenefitGroupId(pbgId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(planBenefitGroupRepository, times(1)).findPlanBenefitGroupWithBenefits(pbgId);
    }
}

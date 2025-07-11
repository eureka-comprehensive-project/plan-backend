package com.comprehensive.eureka.plan.controller;

import com.comprehensive.eureka.plan.dto.BenefitDto;
import com.comprehensive.eureka.plan.dto.PlanBenefitDto;
import com.comprehensive.eureka.plan.dto.PlanDto;
import com.comprehensive.eureka.plan.dto.base.BaseResponseDto;
import com.comprehensive.eureka.plan.dto.request.GetBenefitGroupsRequestDto;
import com.comprehensive.eureka.plan.dto.request.GetPlanBenefitGroupIdRequestDto;
import com.comprehensive.eureka.plan.dto.request.PlanFilterRequestDto;
import com.comprehensive.eureka.plan.dto.request.PlanSearchRequestDto;
import com.comprehensive.eureka.plan.dto.response.FilterListResponseDto;
import com.comprehensive.eureka.plan.dto.response.GetBenefitGroupResponseDto;
import com.comprehensive.eureka.plan.dto.response.GetPlanBenefitGroupIdResponseDto;
import com.comprehensive.eureka.plan.dto.response.PlanFilterResponseDto;
import com.comprehensive.eureka.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/plan")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @PostMapping("/register")
    public BaseResponseDto<PlanDto> registerPlan(@RequestBody PlanDto planDto) {
        PlanDto createdPlan = planService.createPlan(planDto);
        return BaseResponseDto.success(createdPlan);
    }

    @GetMapping("/")
    public BaseResponseDto<List<PlanDto>> getAllPlans() {
        List<PlanDto> plans = planService.getAllPlans();
        return BaseResponseDto.success(plans);
    }

    @PutMapping("/{planId}")
    public BaseResponseDto<PlanDto> updatePlan(
            @PathVariable Integer planId,
            @RequestBody PlanDto planDto) {
        PlanDto updatedPlan = planService.updatePlan(planId, planDto);
        return BaseResponseDto.success(updatedPlan);
    }

    @GetMapping("/{planId}")
    public BaseResponseDto<PlanDto> getPlanById(@PathVariable Integer planId) {
        PlanDto plan = planService.getPlanById(planId);
        return BaseResponseDto.success(plan);
    }

    @GetMapping("/{planId}/benefits")
    public BaseResponseDto<List<BenefitDto>> getBenefitsForPlan(@PathVariable Integer planId) {
        List<BenefitDto> benefits = planService.getAllBenefitsByPlanId(planId);
        return BaseResponseDto.success(benefits);
    }

    @PostMapping("/plan-benefit")
    public BaseResponseDto<List<PlanBenefitDto>> getPlansByPlanBenefitIds(
            @RequestBody List<Long> planBenefitIds) {
        List<PlanBenefitDto> plans = planService.getPlansByPlanBenefitIds(planBenefitIds);
        return BaseResponseDto.success(plans);
    }

    @PostMapping("/filter")
    public BaseResponseDto<List<PlanFilterResponseDto>> getFilteredPlans(
            @RequestBody PlanFilterRequestDto filterRequest) {

        List<PlanFilterResponseDto> plans = planService.getFilteredPlans(filterRequest);
        return BaseResponseDto.success(plans);
    }

    @GetMapping("/category/{categoryId}")
    public BaseResponseDto<List<PlanFilterResponseDto>> getPlansByCategory(
            @PathVariable Long categoryId) {

        PlanFilterRequestDto filterRequest = new PlanFilterRequestDto();
        if (categoryId == 0) {
            filterRequest.setAllCategoriesSelected(true);
        } else {
            filterRequest.setCategoryIds(List.of(categoryId));
        }

        List<PlanFilterResponseDto> plans = planService.getFilteredPlans(filterRequest);
        return BaseResponseDto.success(plans);
    }

    @PostMapping("/filter/count")
    public BaseResponseDto<Integer> countPlansWithFilter(@RequestBody PlanFilterRequestDto requestDto) {
        int count = planService.countPlansWithFilter(requestDto);
        return BaseResponseDto.success(count);
    }

    @GetMapping("/{planId}/benefit-group/{benefitGroupId}/exists")
    public BaseResponseDto<Boolean> checkPlanHasBenefitGroup(
            @PathVariable Integer planId,
            @PathVariable Long benefitGroupId) {

        boolean hasBenefitGroup = planService.checkPlanHasBenefitGroup(planId, benefitGroupId);
        return BaseResponseDto.success(hasBenefitGroup);
    }

    @PostMapping("/search")
    public BaseResponseDto<List<PlanDto>> searchPlansByPlanName(
            @RequestBody PlanSearchRequestDto requestDto) {
        List<PlanDto> plans = planService.findPlansByPlanNameContaining(requestDto.getPlanName());
        return BaseResponseDto.success(plans);
    }

    @PostMapping("/filter/list")
    public BaseResponseDto<List<FilterListResponseDto>> getFilteredList(
            @RequestBody PlanFilterRequestDto filterRequest) {

        List<FilterListResponseDto> plans = planService.getFilteredList(filterRequest);
        return BaseResponseDto.success(plans);
    }

    @PostMapping("/benefit-group")
    public BaseResponseDto<GetBenefitGroupResponseDto> getBenefitGroups(@RequestBody GetBenefitGroupsRequestDto requestDto) {
        Long benefitGroupsByPlanIds = planService.getBenefitGroupsByPlanIds(requestDto.getBenefitIds());
        GetBenefitGroupResponseDto getBenefitGroupResponseDto = GetBenefitGroupResponseDto.builder()
                .id(benefitGroupsByPlanIds)
                .build();
        return BaseResponseDto.success(getBenefitGroupResponseDto);
    }

    @PostMapping("/findPlanBenefitGroupId")
    public BaseResponseDto<GetPlanBenefitGroupIdResponseDto> getPlanBenefitGroupId(@RequestBody GetPlanBenefitGroupIdRequestDto requestDto) {
        Long planBenefitGroupId = planService.getPlanBenefitGroupId(requestDto);
        GetPlanBenefitGroupIdResponseDto getPlanBenefitGroupIdResponseDto = GetPlanBenefitGroupIdResponseDto.builder()
                .id(planBenefitGroupId)
                .build();
        BaseResponseDto<GetPlanBenefitGroupIdResponseDto> success = BaseResponseDto.success(getPlanBenefitGroupIdResponseDto);
        return success;
    }
}

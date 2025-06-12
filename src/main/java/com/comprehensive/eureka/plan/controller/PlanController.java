package com.comprehensive.eureka.plan.controller;

import com.comprehensive.eureka.plan.dto.BenefitDto;
import com.comprehensive.eureka.plan.dto.PlanDto;
import com.comprehensive.eureka.plan.dto.base.BaseResponseDto;
import com.comprehensive.eureka.plan.dto.request.PlanFilterRequest;
import com.comprehensive.eureka.plan.dto.response.PlanResponseDto;
import com.comprehensive.eureka.plan.service.PlanFilterService;
import com.comprehensive.eureka.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/plan")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;
    private final PlanFilterService planFilterService;

    @PostMapping
    public BaseResponseDto<PlanDto> registerPlan(@RequestBody PlanDto planDto) {
        PlanDto createdPlan = planService.createPlan(planDto);
        return BaseResponseDto.success(createdPlan);
    }

    @GetMapping
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

    @PostMapping("/filter")
    public ResponseEntity<List<PlanResponseDto>> getFilteredPlans(
            @RequestBody PlanFilterRequest filterRequest) {

        List<PlanResponseDto> plans = planFilterService.getFilteredPlans(filterRequest);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<PlanResponseDto>> getPlansByCategory(
            @PathVariable Long categoryId) {

        PlanFilterRequest filterRequest = new PlanFilterRequest();
        if (categoryId == 0) { // 전체 카테고리
            filterRequest.setAllCategoriesSelected(true);
        } else {
            filterRequest.setCategoryIds(List.of(categoryId));
        }

        List<PlanResponseDto> plans = planFilterService.getFilteredPlans(filterRequest);
        return ResponseEntity.ok(plans);
    }
}

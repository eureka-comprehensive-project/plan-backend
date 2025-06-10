package com.comprehensive.eureka.plan.controller;

import com.comprehensive.eureka.plan.dto.PlanDto;
import com.comprehensive.eureka.plan.dto.base.BaseResponseDto;
import com.comprehensive.eureka.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

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
}

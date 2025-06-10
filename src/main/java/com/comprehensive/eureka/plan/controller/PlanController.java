package com.comprehensive.eureka.plan.controller;

import com.comprehensive.eureka.plan.dto.PlanDto;
import com.comprehensive.eureka.plan.dto.base.BaseResponseDto;
import com.comprehensive.eureka.plan.entity.Plan;
import com.comprehensive.eureka.plan.exception.ErrorCode;
import com.comprehensive.eureka.plan.service.PlanService;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

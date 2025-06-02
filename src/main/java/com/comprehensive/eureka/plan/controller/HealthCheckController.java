package com.comprehensive.eureka.plan.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/plan")
@RestController
public class HealthCheckController {

    @GetMapping("/healthCheck")
    public String healthCheck() {
        return "plan 25.06.02";
    }
}

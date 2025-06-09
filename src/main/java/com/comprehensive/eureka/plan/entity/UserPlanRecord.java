package com.comprehensive.eureka.plan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_plan_record")
public class UserPlanRecord {

    @Id
    private Long userPlanRecordId;

    private Long userId;

    private LocalDate contractDate;

    private LocalDate contractExpiryDate;

    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_benefit_id")
    private PlanBenefitGroup planBenefitGroup;
}


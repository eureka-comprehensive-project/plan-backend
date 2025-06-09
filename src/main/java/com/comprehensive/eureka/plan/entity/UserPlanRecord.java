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
    @Column(name = "고객기록 ID")
    private Long userPlanRecordId;

    @Column(name = "사용자 ID")
    private Long userId;

    @Column(name = "계약일")
    private LocalDate contractDate;

    @Column(name = "계약만료일")
    private LocalDate contractExpiryDate;

    @Column(name = "활성화")
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "요금제혜택 ID")
    private PlanBenefitGroup planBenefitGroup;
}


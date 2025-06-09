package com.comprehensive.eureka.plan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "plan_benefit_group")
public class PlanBenefitGroup {

    @Id
    @Column(name = "요금제혜택ID")
    private Long planBenefitId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "요금제ID")
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "혜택모음ID")
    private BenefitGroup benefitGroup;

    @OneToMany(mappedBy = "planBenefitGroup")
    private List<UserPlanRecord> userPlanRecords = new ArrayList<>();

}
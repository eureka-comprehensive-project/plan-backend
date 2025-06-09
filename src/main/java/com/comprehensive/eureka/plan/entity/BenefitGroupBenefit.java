package com.comprehensive.eureka.plan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "benefit_group_benefit")
public class BenefitGroupBenefit {

    @Id
    private Long benefitGroupBenefitId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benefit_group_id")
    private BenefitGroup benefitGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benefit_id")
    private Benefit benefit;

}

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
    @Column(name = "혜택모음혜택ID")
    private Long benefitGroupBenefitId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "혜택모음ID")
    private BenefitGroup benefitGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "혜택ID")
    private Benefit benefit;

}

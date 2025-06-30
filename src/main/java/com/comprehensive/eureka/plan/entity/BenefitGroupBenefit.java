package com.comprehensive.eureka.plan.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "benefitGroupBenefitId")
@Table(name = "benefit_group_benefit")
public class BenefitGroupBenefit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long benefitGroupBenefitId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benefit_group_id")
    private BenefitGroup benefitGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benefit_id")
    private Benefit benefit;

}

package com.comprehensive.eureka.plan.entity;

import com.comprehensive.eureka.plan.entity.enums.BenefitType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "benefitId")
@Table(name = "benefit")
public class Benefit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "benefit_id")
    private Long benefitId;

    private String benefitName;

    @Enumerated(EnumType.STRING)
    private BenefitType benefitType;

    @OneToMany(mappedBy = "benefit")
    private List<BenefitGroupBenefit> benefitGroupBenefits = new ArrayList<>();
}

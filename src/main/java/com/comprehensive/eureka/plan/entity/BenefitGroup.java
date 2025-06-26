package com.comprehensive.eureka.plan.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "benefitGroupId")
@Table(name = "benefit_group")
public class BenefitGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "benefit_group_id")
    private Long benefitGroupId;

    private String description;

    @OneToMany(mappedBy = "benefitGroup")
    @Builder.Default
    private List<PlanBenefitGroup> planBenefitGroups = new ArrayList<>();

    @OneToMany(mappedBy = "benefitGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<BenefitGroupBenefit> benefitGroupBenefits = new HashSet<>();
}
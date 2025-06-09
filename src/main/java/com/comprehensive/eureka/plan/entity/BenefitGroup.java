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
@Table(name = "benefit_group")
public class BenefitGroup {

    @Id
    @Column(name = "혜택모음ID")
    private Long benefitGroupId;

    @Column(name = "설명")
    private String description;

    @OneToMany(mappedBy = "benefitGroup")
    private List<PlanBenefitGroup> planBenefitGroups = new ArrayList<>();

    @OneToMany(mappedBy = "benefitGroup")
    private List<BenefitGroupBenefit> benefitGroupBenefits = new ArrayList<>();

}
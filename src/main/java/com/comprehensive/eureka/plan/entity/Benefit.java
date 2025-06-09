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
@Table(name = "benefit")
public class Benefit {

    @Id
    @Column(name = "혜택ID")
    private Long benefitId;

    @Column(name = "혜택명", length = 20)
    private String benefitName;

    @Enumerated(EnumType.STRING)
    @Column(name = "혜택타입")
    private BenefitType benefitType;

    @OneToMany(mappedBy = "benefit")
    private List<BenefitGroupBenefit> benefitGroupBenefits = new ArrayList<>();
}

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
@EqualsAndHashCode(of = "planId")
@Table(name = "plan")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long planId;

    private String planName;

    private Integer monthlyFee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_allowance_id")
    private DataAllowances dataAllowances;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voice_call_id")
    private VoiceCall voiceCall;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_data_id")
    private SharedData sharedData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private PlanCategory planCategory;

    @OneToMany(mappedBy = "plan")
    @Builder.Default
    private Set<PlanBenefitGroup> planBenefitGroups = new HashSet<>();

}

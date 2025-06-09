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
@Table(name = "plan")
public class Plan {

    @Id
    @Column(name = "plan_id")
    private Integer planId;

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
    private List<PlanBenefitGroup> planBenefitGroups = new ArrayList<>();

}

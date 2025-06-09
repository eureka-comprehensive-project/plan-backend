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
    @Column(name = "요금제ID")
    private Integer planId;

    @Column(name = "요금제 이름", length = 20)
    private String planName;

    @Column(name = "월정액")
    private Integer monthlyFee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "데이터 제공량 ID")
    private DataAllowances dataAllowances;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "음성통화 ID")
    private VoiceCall voiceCall;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "테더링 쉐어링 ID")
    private SharedData sharedData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "카테고리 ID")
    private PlanCategory planCategory;

    @OneToMany(mappedBy = "plan")
    private List<PlanBenefitGroup> planBenefitGroups = new ArrayList<>();

}

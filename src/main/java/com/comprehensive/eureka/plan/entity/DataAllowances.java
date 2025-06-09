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
@Table(name = "data_allowances")
public class DataAllowances {

    @Id
    @Column(name = "데이터제공량ID")
    private Integer dataAllowanceId;

    @Column(name = "데이터 수치")
    private Integer dataAmount;

    @Column(name = "데이터 단위", length = 2)
    private String dataUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "일/월")
    private DataPeriod dataPeriod;

    @OneToMany(mappedBy = "dataAllowances")
    private List<Plan> plans = new ArrayList<>();
}


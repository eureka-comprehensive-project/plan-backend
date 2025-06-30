package com.comprehensive.eureka.plan.entity;

import com.comprehensive.eureka.plan.entity.enums.DataPeriod;
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
@EqualsAndHashCode(of = "dataAllowanceId")
@Table(name = "data_allowances")
public class DataAllowances {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "data_allowance_id")
    private Long dataAllowanceId;

    private Integer dataAmount;

    private String dataUnit;

    @Enumerated(EnumType.STRING)
    private DataPeriod dataPeriod;

    @OneToMany(mappedBy = "dataAllowances")
    private List<Plan> plans = new ArrayList<>();
}


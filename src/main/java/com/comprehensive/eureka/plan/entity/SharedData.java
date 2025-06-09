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
@Table(name = "shared_data")
public class SharedData {

    @Id
    @Column(name = "테더링 쉐어링 ID")
    private Integer sharedDataId;

    @Column(name = "테더링/쉐어링 데이터 수치")
    private Integer tetheringDataAmount;

    @Column(name = "테더링/쉐어링 데이터 단위", length = 2)
    private String tetheringDataUnit;

    @Column(name = "가족데이터 여부")
    private Boolean familyDataAvailable;

    @Column(name = "가족데이터 수치")
    private Integer familyDataAmount;

    @Column(name = "가족데이터 단위", length = 2)
    private String familyDataUnit;

    @OneToMany(mappedBy = "sharedData")
    private List<Plan> plans = new ArrayList<>();
}

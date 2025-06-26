package com.comprehensive.eureka.plan.entity;

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
@EqualsAndHashCode(of = "sharedDataId")
@Table(name = "shared_data")
public class SharedData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shared_data_id")
    private Long sharedDataId;

    private Integer tetheringDataAmount;

    private String tetheringDataUnit;

    private Boolean familyDataAvailable;

    private Integer familyDataAmount;

    private String familyDataUnit;

    @OneToMany(mappedBy = "sharedData")
    private List<Plan> plans = new ArrayList<>();
}

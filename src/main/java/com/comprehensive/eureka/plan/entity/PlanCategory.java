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
@Table(name = "plan_category")
public class PlanCategory {

    @Id
    @Column(name = "카테고리 ID")
    private Long categoryId;

    @Column(name = "카테고리명")
    private String categoryName;

    @OneToMany(mappedBy = "planCategory")
    private List<Plan> plans = new ArrayList<>();
}

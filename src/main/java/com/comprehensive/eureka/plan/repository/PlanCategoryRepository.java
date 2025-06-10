package com.comprehensive.eureka.plan.repository;

import com.comprehensive.eureka.plan.entity.PlanCategory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanCategoryRepository extends JpaRepository<PlanCategory, Integer> {
    Optional<PlanCategory> findByCategoryName(String categoryName);
}

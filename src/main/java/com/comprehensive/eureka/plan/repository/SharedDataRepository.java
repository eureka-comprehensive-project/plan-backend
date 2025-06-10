package com.comprehensive.eureka.plan.repository;

import com.comprehensive.eureka.plan.entity.SharedData;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SharedDataRepository extends JpaRepository<SharedData, Integer> {
    Optional<SharedData> findByTetheringDataAmountAndTetheringDataUnitAndFamilyDataAmountAndFamilyDataUnit(
            Integer tetheringDataAmount, String tetheringDataUnit, Integer familyDataAmount, String familyDataUnit
    );
}

package com.comprehensive.eureka.plan.repository;

import com.comprehensive.eureka.plan.entity.DataAllowances;
import com.comprehensive.eureka.plan.entity.enums.DataPeriod;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataAllowanceRepository extends JpaRepository<DataAllowances, Integer> {
    Optional<DataAllowances> findByDataAmountAndDataUnitAndDataPeriod(
            Integer dataAmount, String dataUnit, DataPeriod dataPeriod);
}

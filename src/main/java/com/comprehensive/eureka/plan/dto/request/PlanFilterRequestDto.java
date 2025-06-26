package com.comprehensive.eureka.plan.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PlanFilterRequestDto {

    private List<Long> categoryIds;
    private boolean allCategoriesSelected;

    private List<String> priceRanges;
    private boolean anyPriceSelected;

    private List<String> dataOptions;
    private boolean anyDataSelected;

    private List<Long> benefitIds;
    private boolean noBenefitsSelected;
}

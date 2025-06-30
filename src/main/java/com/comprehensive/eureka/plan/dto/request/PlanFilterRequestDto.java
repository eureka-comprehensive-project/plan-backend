package com.comprehensive.eureka.plan.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

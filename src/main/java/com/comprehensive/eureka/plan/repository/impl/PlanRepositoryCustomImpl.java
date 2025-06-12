package com.comprehensive.eureka.plan.repository.impl;

import com.comprehensive.eureka.plan.dto.request.PlanFilterRequestDto;
import com.comprehensive.eureka.plan.entity.Plan;
import com.comprehensive.eureka.plan.entity.enums.DataPeriod;
import com.comprehensive.eureka.plan.repository.PlanRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.comprehensive.eureka.plan.entity.QBenefit.benefit;
import static com.comprehensive.eureka.plan.entity.QBenefitGroup.benefitGroup;
import static com.comprehensive.eureka.plan.entity.QBenefitGroupBenefit.benefitGroupBenefit;
import static com.comprehensive.eureka.plan.entity.QDataAllowances.dataAllowances;
import static com.comprehensive.eureka.plan.entity.QPlan.plan;
import static com.comprehensive.eureka.plan.entity.QPlanBenefitGroup.planBenefitGroup;
import static com.comprehensive.eureka.plan.entity.QPlanCategory.planCategory;
import static com.comprehensive.eureka.plan.entity.QSharedData.sharedData;
import static com.comprehensive.eureka.plan.entity.QVoiceCall.voiceCall;

@Repository
@RequiredArgsConstructor
public class PlanRepositoryCustomImpl implements PlanRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Plan> findPlansWithFilter(PlanFilterRequestDto filterRequest) {
        BooleanBuilder builder = new BooleanBuilder();

        // 카테고리 필터
        builder.and(categoryFilter(filterRequest));

        // 요금 범위 필터
        builder.and(priceRangeFilter(filterRequest));

        // 데이터 옵션 필터
        builder.and(dataOptionFilter(filterRequest));

        // 혜택 필터
        builder.and(benefitFilter(filterRequest));

        return queryFactory
                .selectFrom(plan)
                .distinct()
                .leftJoin(plan.planCategory, planCategory).fetchJoin()
                .leftJoin(plan.dataAllowances, dataAllowances).fetchJoin()
                .leftJoin(plan.voiceCall, voiceCall).fetchJoin()
                .leftJoin(plan.sharedData, sharedData).fetchJoin()
                .leftJoin(plan.planBenefitGroups, planBenefitGroup).fetchJoin()
                .leftJoin(planBenefitGroup.benefitGroup, benefitGroup).fetchJoin()
                .leftJoin(benefitGroup.benefitGroupBenefits, benefitGroupBenefit).fetchJoin()
                .leftJoin(benefitGroupBenefit.benefit, benefit).fetchJoin()
                .where(builder)
                .fetch();
    }

    private BooleanExpression categoryFilter(PlanFilterRequestDto filterRequest) {
        // 전체 카테고리 선택시 필터 적용하지 않음
        if (filterRequest.isAllCategoriesSelected() ||
                filterRequest.getCategoryIds() == null ||
                filterRequest.getCategoryIds().isEmpty()) {
            return null;
        }

        return plan.planCategory.categoryId.in(filterRequest.getCategoryIds());
    }

    private Predicate priceRangeFilter(PlanFilterRequestDto filterRequest) {
        // 상관없어요 선택시 필터 적용하지 않음
        if (filterRequest.isAnyPriceSelected() ||
                filterRequest.getPriceRanges() == null ||
                filterRequest.getPriceRanges().isEmpty()) {
            return null;
        }

        BooleanBuilder priceBuilder = new BooleanBuilder();

        for (String range : filterRequest.getPriceRanges()) {
            switch (range) {
                case "~5":
                    priceBuilder.or(plan.monthlyFee.loe(50000));
                    break;
                case "6~8":
                    priceBuilder.or(plan.monthlyFee.between(60000, 80000));
                    break;
                case "9~":
                    priceBuilder.or(plan.monthlyFee.goe(90000));
                    break;
            }
        }

        return priceBuilder;
    }

    private Predicate dataOptionFilter(PlanFilterRequestDto filterRequest) {
        // 상관없어요 선택시 필터 적용하지 않음
        if (filterRequest.isAnyDataSelected() ||
                filterRequest.getDataOptions() == null ||
                filterRequest.getDataOptions().isEmpty()) {
            return null;
        }

        BooleanBuilder dataBuilder = new BooleanBuilder();

        for (String option : filterRequest.getDataOptions()) {
            switch (option) {
                case "small":
                    // 소용량: 월간 10GB 이하 또는 일간 환산시 10GB 이하
                    dataBuilder.or(
                            plan.dataAllowances.isNull()
                                    .or(plan.dataAllowances.dataAmount.eq(0).not()
                                            .and(plan.dataAllowances.dataPeriod.eq(DataPeriod.MONTH)
                                                    .and(plan.dataAllowances.dataAmount.loe(10)))
                                            .or(plan.dataAllowances.dataPeriod.eq(DataPeriod.DAY)
                                                    .and(plan.dataAllowances.dataAmount.multiply(30).loe(10))))
                    );
                    break;
                case "large":
                    // 대용량: 무제한(0) 또는 월간 10GB 초과 또는 일간 환산시 10GB 초과
                    dataBuilder.or(
                            plan.dataAllowances.dataAmount.eq(0)
                                    .or(plan.dataAllowances.dataPeriod.eq(DataPeriod.MONTH)
                                            .and(plan.dataAllowances.dataAmount.gt(10)))
                                    .or(plan.dataAllowances.dataPeriod.eq(DataPeriod.DAY)
                                            .and(plan.dataAllowances.dataAmount.multiply(30).gt(10)))
                    );
                    break;
            }
        }

        return dataBuilder;
    }

    private BooleanExpression benefitFilter(PlanFilterRequestDto filterRequest) {
        // 혜택 필요없어요 선택시 혜택이 없는 요금제만
        if (filterRequest.isNoBenefitsSelected()) {
            return queryFactory
                    .selectFrom(planBenefitGroup)
                    .where(planBenefitGroup.plan.eq(plan))
                    .notExists();
        }

        // 혜택 필터가 없으면 모든 요금제
        if (filterRequest.getBenefitIds() == null || filterRequest.getBenefitIds().isEmpty()) {
            return null;
        }

        // 선택한 혜택 중 하나라도 포함하는 요금제
        return queryFactory
                .selectFrom(planBenefitGroup)
                .join(planBenefitGroup.benefitGroup, benefitGroup)
                .join(benefitGroup.benefitGroupBenefits, benefitGroupBenefit)
                .join(benefitGroupBenefit.benefit, benefit)
                .where(planBenefitGroup.plan.eq(plan)
                        .and(benefit.benefitId.in(filterRequest.getBenefitIds())))
                .exists();
    }
}

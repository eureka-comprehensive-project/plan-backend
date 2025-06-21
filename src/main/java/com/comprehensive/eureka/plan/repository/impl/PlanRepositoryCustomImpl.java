package com.comprehensive.eureka.plan.repository.impl;

import com.comprehensive.eureka.plan.dto.request.GetPlanBenefitGroupIdRequestDto;
import com.comprehensive.eureka.plan.dto.request.PlanFilterRequestDto;
import com.comprehensive.eureka.plan.dto.response.FilterListResponseDto;
import com.comprehensive.eureka.plan.entity.BenefitGroup;
import com.comprehensive.eureka.plan.entity.Plan;
import com.comprehensive.eureka.plan.entity.PlanBenefitGroup;
import com.comprehensive.eureka.plan.entity.QBenefitGroupBenefit;
import com.comprehensive.eureka.plan.entity.enums.DataPeriod;
import com.comprehensive.eureka.plan.repository.PlanRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final JPAQueryFactory jpaQueryFactory;

    private static final int UNLIMITED_DATA_AMOUNT = 99999;
    private static final int SMALL_DATA_THRESHOLD_GB = 10;
    private static final int LARGE_DATA_THRESHOLD_MB = SMALL_DATA_THRESHOLD_GB * 1000;

    @Override
    public List<Plan> findPlansWithFilter(PlanFilterRequestDto filterRequest) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(categoryFilter(filterRequest));

        builder.and(priceRangeFilter(filterRequest));

        builder.and(dataOptionFilter(filterRequest));

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
        if (filterRequest.isAllCategoriesSelected() ||
                filterRequest.getCategoryIds() == null ||
                filterRequest.getCategoryIds().isEmpty()) {
            return null;
        }

        return plan.planCategory.categoryId.in(filterRequest.getCategoryIds());
    }

    private Predicate priceRangeFilter(PlanFilterRequestDto filterRequest) {
        if (filterRequest.isAnyPriceSelected() ||
                filterRequest.getPriceRanges() == null ||
                filterRequest.getPriceRanges().isEmpty()) {
            return null;
        }

        BooleanBuilder priceBuilder = new BooleanBuilder();

        for (String range : filterRequest.getPriceRanges()) {
            switch (range) {
                case "~5만원대":
                    priceBuilder.or(plan.monthlyFee.loe(50000));
                    break;
                case "6~8만원대":
                    priceBuilder.or(plan.monthlyFee.between(60000, 80000));
                    break;
                case "9만원대~":
                    priceBuilder.or(plan.monthlyFee.goe(90000));
                    break;
            }
        }

        return priceBuilder;
    }

    public Predicate dataOptionFilter(PlanFilterRequestDto filterRequest) {
        if (filterRequest.isAnyDataSelected() ||
                filterRequest.getDataOptions() == null ||
                filterRequest.getDataOptions().isEmpty()) {
            return null;
        }

        BooleanBuilder dataBuilder = new BooleanBuilder();

        for (String option : filterRequest.getDataOptions()) {
            switch (option) {
                case "소용량":
                    dataBuilder.or(
                            plan.dataAllowances.isNull()
                                    .or(plan.dataAllowances.dataAmount.eq(0)
                                            .or(plan.dataAllowances.dataUnit.eq("MB")
                                                    .and(plan.dataAllowances.dataPeriod.eq(DataPeriod.MONTH)
                                                            .and(plan.dataAllowances.dataAmount.loe(LARGE_DATA_THRESHOLD_MB)))
                                                    .or(plan.dataAllowances.dataPeriod.eq(DataPeriod.DAY)
                                                            .and(plan.dataAllowances.dataAmount.multiply(30).loe(LARGE_DATA_THRESHOLD_MB))))
                                            .or(plan.dataAllowances.dataUnit.eq("GB")
                                                    .and(plan.dataAllowances.dataPeriod.eq(DataPeriod.MONTH)
                                                            .and(plan.dataAllowances.dataAmount.loe(SMALL_DATA_THRESHOLD_GB)))
                                                    .or(plan.dataAllowances.dataPeriod.eq(DataPeriod.DAY)
                                                            .and(plan.dataAllowances.dataAmount.multiply(30).loe(SMALL_DATA_THRESHOLD_GB))))
                                    )
                    );
                    break;
                case "대용량":
                    dataBuilder.or(
                            plan.dataAllowances.dataAmount.ne(UNLIMITED_DATA_AMOUNT)
                                    .and(plan.dataAllowances.isNotNull())
                                    .and(
                                            plan.dataAllowances.dataPeriod.eq(DataPeriod.MONTH)
                                                    .and(plan.dataAllowances.dataUnit.eq("GB"))
                                                    .and(plan.dataAllowances.dataAmount.gt(SMALL_DATA_THRESHOLD_GB))
                                                    .or(plan.dataAllowances.dataPeriod.eq(DataPeriod.DAY)
                                                            .and(plan.dataAllowances.dataUnit.eq("GB"))
                                                            .and(plan.dataAllowances.dataAmount.multiply(30).gt(SMALL_DATA_THRESHOLD_GB)))
                                                    .or(plan.dataAllowances.dataPeriod.eq(DataPeriod.MONTH)
                                                            .and(plan.dataAllowances.dataUnit.eq("MB"))
                                                            .and(plan.dataAllowances.dataAmount.gt(LARGE_DATA_THRESHOLD_MB)))
                                                    .or(plan.dataAllowances.dataPeriod.eq(DataPeriod.DAY)
                                                            .and(plan.dataAllowances.dataUnit.eq("MB"))
                                                            .and(plan.dataAllowances.dataAmount.multiply(30).gt(LARGE_DATA_THRESHOLD_MB)))
                                    )
                    );
                    break;
                case "unlimited":
                    dataBuilder.or(plan.dataAllowances.dataAmount.eq(UNLIMITED_DATA_AMOUNT));
                    break;
            }
        }

        return dataBuilder;
    }

    private BooleanExpression benefitFilter(PlanFilterRequestDto filterRequest) {
        if (filterRequest.isNoBenefitsSelected()) {
            return queryFactory
                    .selectFrom(planBenefitGroup)
                    .where(planBenefitGroup.plan.eq(plan))
                    .notExists();
        }

        if (filterRequest.getBenefitIds() == null || filterRequest.getBenefitIds().isEmpty()) {
            return null;
        }

        return queryFactory
                .selectFrom(planBenefitGroup)
                .join(planBenefitGroup.benefitGroup, benefitGroup)
                .join(benefitGroup.benefitGroupBenefits, benefitGroupBenefit)
                .join(benefitGroupBenefit.benefit, benefit)
                .where(planBenefitGroup.plan.eq(plan)
                        .and(benefit.benefitId.in(filterRequest.getBenefitIds())))
                .exists();
    }

    @Override
    public int countPlansWithFilter(PlanFilterRequestDto filterRequest) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(categoryFilter(filterRequest));
        builder.and(priceRangeFilter(filterRequest));
        builder.and(dataOptionFilter(filterRequest));
        builder.and(benefitFilter(filterRequest));

        Long count = queryFactory
                .select(plan.count())
                .from(plan)
                .leftJoin(plan.planCategory, planCategory)
                .leftJoin(plan.dataAllowances, dataAllowances)
                .leftJoin(plan.voiceCall, voiceCall)
                .leftJoin(plan.sharedData, sharedData)
                .leftJoin(plan.planBenefitGroups, planBenefitGroup)
                .leftJoin(planBenefitGroup.benefitGroup, benefitGroup)
                .leftJoin(benefitGroup.benefitGroupBenefits, benefitGroupBenefit)
                .leftJoin(benefitGroupBenefit.benefit, benefit)
                .where(builder)
                .fetchOne();

        return count != null ? count.intValue() : 0;
    }

    @Override
    public List<FilterListResponseDto> getFilteredList(PlanFilterRequestDto filterRequest) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(categoryFilter(filterRequest));

        builder.and(priceRangeFilter(filterRequest));

        builder.and(dataOptionFilter(filterRequest));

        builder.and(benefitFilter(filterRequest));

        List<Plan> fetch = queryFactory
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

        return fetch.stream()
                .map(FilterListResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<BenefitGroup> findBenefitGroupIdsByAllBenefitIds(List<Long> benefitIds) {
        if (benefitIds == null || benefitIds.isEmpty()) {
            return Optional.empty();
        }

        QBenefitGroupBenefit benefitGroupBenefit = QBenefitGroupBenefit.benefitGroupBenefit;
        long benefitIdsSize = benefitIds.size();

        // 1. (서브쿼리) Benefit의 총 개수가 benefitIds의 크기와 정확히 일치하는 benefitGroup의 ID를 찾습니다.
        List<Long> exactMatchGroupIds = queryFactory
                .select(benefitGroupBenefit.benefitGroup.benefitGroupId)
                .from(benefitGroupBenefit)
                .groupBy(benefitGroupBenefit.benefitGroup.benefitGroupId)
                .having(benefitGroupBenefit.benefit.count().eq(benefitIdsSize))
                .fetch();

        if (exactMatchGroupIds.isEmpty()) {
            return Optional.empty();
        }

        // 2. (메인쿼리) 서브쿼리 결과를 사용하여 정확한 BenefitGroup을 찾습니다.
        BenefitGroup result = queryFactory
                .select(benefitGroupBenefit.benefitGroup)
                .from(benefitGroupBenefit)
                .where(benefitGroupBenefit.benefit.benefitId.in(benefitIds)
                        // 2-2. 그리고 서브쿼리에서 찾은 "총 개수가 일치하는" 그룹 ID 목록에 포함되어야 함
                        .and(benefitGroupBenefit.benefitGroup.benefitGroupId.in(exactMatchGroupIds)))
                .groupBy(benefitGroupBenefit.benefitGroup)
                // 2-3. 포함된 benefit의 개수가 일치하는지 다시 확인 (신뢰성 확보)
                .having(benefitGroupBenefit.benefit.benefitId.countDistinct().eq(benefitIdsSize))
                .fetchFirst();

        return Optional.ofNullable(result);
    }

    @Override
    public PlanBenefitGroup getPlanBenefitGroupId(GetPlanBenefitGroupIdRequestDto requestDto) {
        return jpaQueryFactory.select(planBenefitGroup)
                .from(planBenefitGroup)
                .where(
                        planBenefitGroup.plan.planId.eq(Long.valueOf(requestDto.getPlanId())),
                        planBenefitGroup.benefitGroup.benefitGroupId.eq(requestDto.getBenefitGroupId())
                )
                .fetchOne();
    }

}

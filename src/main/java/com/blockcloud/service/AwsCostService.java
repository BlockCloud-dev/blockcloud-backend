package com.blockcloud.service;

import com.blockcloud.dto.ResponseDto.CostForecastResponse;
import com.blockcloud.dto.ResponseDto.CostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;
import software.amazon.awssdk.services.costexplorer.model.*;
import com.blockcloud.exception.CommonException;
import com.blockcloud.exception.error.ErrorCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * AWS Cost Explorer API를 사용한 비용 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AwsCostService {

    private final CostExplorerClient costExplorerClient;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 전체 비용 조회 (일별)
     */
    @Cacheable(value = "totalCost", key = "#startDate + '-' + #endDate")
    public CostResponse getTotalCost(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching total cost from AWS for period: {} to {}", startDate, endDate);

        try {
            GetCostAndUsageRequest request = GetCostAndUsageRequest.builder()
                    .timePeriod(DateInterval.builder()
                            .start(startDate.format(DATE_FORMATTER))
                            .end(endDate.format(DATE_FORMATTER))
                            .build())
                    .granularity(Granularity.DAILY)
                    .metrics("UnblendedCost")
                    .build();

            GetCostAndUsageResponse response = costExplorerClient.getCostAndUsage(request);

        BigDecimal totalCost = BigDecimal.ZERO;
        List<CostResponse.DailyCost> dailyCosts = new ArrayList<>();

        for (ResultByTime result : response.resultsByTime()) {
            String date = result.timePeriod().start();
            String costAmount = result.total().get("UnblendedCost").amount();
            
            // null 안전성을 위한 BigDecimal 생성
            BigDecimal cost = costAmount != null ? new BigDecimal(costAmount) : BigDecimal.ZERO;
            
            totalCost = totalCost.add(cost);
            
            dailyCosts.add(CostResponse.DailyCost.builder()
                    .date(LocalDate.parse(date, DATE_FORMATTER))
                    .cost(cost)
                    .build());
        }

            String currency = response.resultsByTime().isEmpty() ? "USD" 
                    : response.resultsByTime().get(0).total().get("UnblendedCost").unit();

            return CostResponse.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .currency(currency)
                    .totalCost(totalCost)
                    .dailyCosts(dailyCosts)
                    .build();
        } catch (Exception e) {
            log.error("Failed to fetch total cost from AWS: {}", e.getMessage(), e);
            log.error("Exception type: {}", e.getClass().getSimpleName());
            log.error("Exception details: ", e);
            throw new CommonException(ErrorCode.AWS_COST_EXPLORER_ERROR);
        }
    }

    /**
     * 서비스별 비용 조회
     */
    @Cacheable(value = "costByService", key = "#startDate + '-' + #endDate")
    public CostResponse getCostByService(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching cost by service from AWS for period: {} to {}", startDate, endDate);

        try {
            GetCostAndUsageRequest request = GetCostAndUsageRequest.builder()
                    .timePeriod(DateInterval.builder()
                            .start(startDate.format(DATE_FORMATTER))
                            .end(endDate.format(DATE_FORMATTER))
                            .build())
                    .granularity(Granularity.MONTHLY)
                    .groupBy(GroupDefinition.builder()
                            .type(GroupDefinitionType.DIMENSION)
                            .key("SERVICE")
                            .build())
                    .metrics("UnblendedCost")
                    .build();

            GetCostAndUsageResponse response = costExplorerClient.getCostAndUsage(request);

            BigDecimal totalCost = BigDecimal.ZERO;
            List<CostResponse.CostByService> costByServices = new ArrayList<>();
            String currency = "USD";

            for (ResultByTime result : response.resultsByTime()) {
                for (Group group : result.groups()) {
                    String serviceName = group.keys().get(0);
                    String costAmount = group.metrics().get("UnblendedCost").amount();
                    String unit = group.metrics().get("UnblendedCost").unit();
                    
                    // null 안전성을 위한 BigDecimal 생성
                    BigDecimal cost = costAmount != null ? new BigDecimal(costAmount) : BigDecimal.ZERO;
                    
                    totalCost = totalCost.add(cost);
                    currency = unit != null ? unit : "USD";
                    
                    costByServices.add(CostResponse.CostByService.builder()
                            .serviceName(serviceName)
                            .cost(cost)
                            .unit(currency)
                            .build());
                }
            }

            // 비용이 높은 순으로 정렬
            costByServices.sort((a, b) -> b.getCost().compareTo(a.getCost()));

            return CostResponse.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .currency(currency)
                    .totalCost(totalCost)
                    .costByServices(costByServices)
                    .build();
        } catch (Exception e) {
            log.error("Failed to fetch cost by service from AWS: {}", e.getMessage(), e);
            log.error("Exception type: {}", e.getClass().getSimpleName());
            log.error("Exception details: ", e);
            throw new CommonException(ErrorCode.AWS_COST_EXPLORER_ERROR);
        }
    }

    /**
     * 특정 서비스의 비용 조회
     */
    @Cacheable(value = "serviceCost", key = "#serviceName + '-' + #startDate + '-' + #endDate")
    public CostResponse getCostBySpecificService(String serviceName, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching cost for service '{}' from AWS for period: {} to {}", 
                serviceName, startDate, endDate);

        try {
            Expression filter = Expression.builder()
                    .dimensions(DimensionValues.builder()
                            .key("SERVICE")
                            .values(serviceName)
                            .build())
                    .build();

            GetCostAndUsageRequest request = GetCostAndUsageRequest.builder()
                    .timePeriod(DateInterval.builder()
                            .start(startDate.format(DATE_FORMATTER))
                            .end(endDate.format(DATE_FORMATTER))
                            .build())
                    .granularity(Granularity.DAILY)
                    .metrics("UnblendedCost")
                    .filter(filter)
                    .build();

            GetCostAndUsageResponse response = costExplorerClient.getCostAndUsage(request);

            BigDecimal totalCost = BigDecimal.ZERO;
            List<CostResponse.DailyCost> dailyCosts = new ArrayList<>();
            String currency = "USD";

            for (ResultByTime result : response.resultsByTime()) {
                String date = result.timePeriod().start();
                String costAmount = result.total().get("UnblendedCost").amount();
                String unit = result.total().get("UnblendedCost").unit();
                
                // null 안전성을 위한 BigDecimal 생성
                BigDecimal cost = costAmount != null ? new BigDecimal(costAmount) : BigDecimal.ZERO;
                currency = unit != null ? unit : "USD";
                
                totalCost = totalCost.add(cost);
                
                dailyCosts.add(CostResponse.DailyCost.builder()
                        .date(LocalDate.parse(date, DATE_FORMATTER))
                        .cost(cost)
                        .build());
            }

            return CostResponse.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .currency(currency)
                    .totalCost(totalCost)
                    .dailyCosts(dailyCosts)
                    .build();
        } catch (Exception e) {
            log.error("Failed to fetch cost for service '{}' from AWS: {}", serviceName, e.getMessage(), e);
            log.error("Exception type: {}", e.getClass().getSimpleName());
            log.error("Exception details: ", e);
            throw new CommonException(ErrorCode.AWS_COST_EXPLORER_ERROR);
        }
    }

    /**
     * 비용 예측 조회 (최대 3개월 일별)
     */
    @Cacheable(value = "costForecast", key = "#startDate + '-' + #endDate")
    public CostForecastResponse getCostForecast(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching cost forecast from AWS for period: {} to {}", startDate, endDate);

        // AWS Cost Explorer 예측은 최대 3개월까지만 지원
        LocalDate maxEndDate = LocalDate.now().plusMonths(3);
        if (endDate.isAfter(maxEndDate)) {
            log.warn("Requested end date {} is after the maximum allowed date {}. Adjusting to maximum allowed date.", 
                    endDate, maxEndDate);
            endDate = maxEndDate;
        }

        // 예측은 미래 날짜에 대해서만 가능
        LocalDate minStartDate = LocalDate.now().plusDays(1);
        if (startDate.isBefore(minStartDate)) {
            log.warn("Requested start date {} is before the minimum allowed date {}. Adjusting to minimum allowed date.", 
                    startDate, minStartDate);
            startDate = minStartDate;
        }

        try {
            GetCostForecastRequest request = GetCostForecastRequest.builder()
                    .timePeriod(DateInterval.builder()
                            .start(startDate.format(DATE_FORMATTER))
                            .end(endDate.format(DATE_FORMATTER))
                            .build())
                    .metric(Metric.UNBLENDED_COST)
                    .granularity(Granularity.DAILY)
                    .build();

            GetCostForecastResponse response = costExplorerClient.getCostForecast(request);

            BigDecimal totalForecast = BigDecimal.ZERO;
            List<CostForecastResponse.ForecastData> forecastDataList = new ArrayList<>();

            for (ForecastResult result : response.forecastResultsByTime()) {
                String date = result.timePeriod().start();
                BigDecimal meanValue = new BigDecimal(result.meanValue());
                
                totalForecast = totalForecast.add(meanValue);
                
                // null 안전성을 위한 BigDecimal 생성 (없으면 응답에서 필드를 제외)
                BigDecimal lowerBound = result.predictionIntervalLowerBound() != null 
                    ? new BigDecimal(result.predictionIntervalLowerBound()) 
                    : null;
                    
                BigDecimal upperBound = result.predictionIntervalUpperBound() != null 
                    ? new BigDecimal(result.predictionIntervalUpperBound()) 
                    : null;
                
                forecastDataList.add(CostForecastResponse.ForecastData.builder()
                        .date(LocalDate.parse(date, DATE_FORMATTER))
                        .meanValue(meanValue)
                        .predictionIntervalLowerBound(lowerBound)
                        .predictionIntervalUpperBound(upperBound)
                        .build());
            }

            return CostForecastResponse.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .currency("USD")
                    .totalForecastedCost(totalForecast)
                    .forecastData(forecastDataList)
                    .build();
        } catch (Exception e) {
            log.error("Failed to fetch cost forecast from AWS: {}", e.getMessage(), e);
            log.error("Exception type: {}", e.getClass().getSimpleName());
            log.error("Exception details: ", e);
            
            if (e.getMessage() != null) {
                String errorMessage = e.getMessage();
                log.error("Error message: {}", errorMessage);
                
                if (errorMessage.contains("Insufficient amount of historical data")) {
                    throw new CommonException(ErrorCode.AWS_INSUFFICIENT_DATA);
                } else if (errorMessage.contains("forecast")) {
                    throw new CommonException(ErrorCode.AWS_FORECAST_LIMIT);
                } else if (errorMessage.contains("historical data beyond 14 months")) {
                    throw new CommonException(ErrorCode.AWS_HISTORICAL_DATA_LIMIT);
                }
            }
            throw new CommonException(ErrorCode.AWS_COST_EXPLORER_ERROR);
        }
    }

    /**
     * 월별 비용 추세 조회 (최대 13개월)
     */
    @Cacheable(value = "monthlyCost", key = "#startDate + '-' + #endDate")
    public CostResponse getMonthlyCost(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching monthly cost from AWS for period: {} to {}", startDate, endDate);

        // AWS Cost Explorer는 최대 13개월 + 현재 월의 데이터만 제공
        LocalDate maxStartDate = LocalDate.now().minusMonths(13).withDayOfMonth(1);
        if (startDate.isBefore(maxStartDate)) {
            log.warn("Requested start date {} is before the maximum allowed date {}. Adjusting to maximum allowed date.", 
                    startDate, maxStartDate);
            startDate = maxStartDate;
        }

        try {
            GetCostAndUsageRequest request = GetCostAndUsageRequest.builder()
                    .timePeriod(DateInterval.builder()
                            .start(startDate.format(DATE_FORMATTER))
                            .end(endDate.format(DATE_FORMATTER))
                            .build())
                    .granularity(Granularity.MONTHLY)
                    .metrics("UnblendedCost")
                    .build();

            GetCostAndUsageResponse response = costExplorerClient.getCostAndUsage(request);

            BigDecimal totalCost = BigDecimal.ZERO;
            List<CostResponse.DailyCost> monthlyCosts = new ArrayList<>();
            String currency = "USD";

            for (ResultByTime result : response.resultsByTime()) {
                String date = result.timePeriod().start();
                String costAmount = result.total().get("UnblendedCost").amount();
                String unit = result.total().get("UnblendedCost").unit();
                
                // null 안전성을 위한 BigDecimal 생성
                BigDecimal cost = costAmount != null ? new BigDecimal(costAmount) : BigDecimal.ZERO;
                currency = unit != null ? unit : "USD";
                
                totalCost = totalCost.add(cost);
                
                monthlyCosts.add(CostResponse.DailyCost.builder()
                        .date(LocalDate.parse(date, DATE_FORMATTER))
                        .cost(cost)
                        .build());
            }

            return CostResponse.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .currency(currency)
                    .totalCost(totalCost)
                    .dailyCosts(monthlyCosts)
                    .build();
        } catch (Exception e) {
            log.error("Failed to fetch monthly cost from AWS: {}", e.getMessage(), e);
            log.error("Exception type: {}", e.getClass().getSimpleName());
            log.error("Exception details: ", e);
            
            if (e.getMessage() != null && e.getMessage().contains("historical data beyond 14 months")) {
                throw new CommonException(ErrorCode.AWS_HISTORICAL_DATA_LIMIT);
            }
            throw new CommonException(ErrorCode.AWS_COST_EXPLORER_ERROR);
        }
    }
}
package com.blockcloud.dto.ResponseDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * AWS 비용 예측 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostForecastResponse {
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    private String currency;
    
    private BigDecimal totalForecastedCost;
    
    private List<ForecastData> forecastData;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(Include.NON_NULL)
    public static class ForecastData {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        
        private BigDecimal meanValue;
        
        private BigDecimal predictionIntervalLowerBound;
        
        private BigDecimal predictionIntervalUpperBound;
    }
}

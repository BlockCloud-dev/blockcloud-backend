package com.blockcloud.dto.ResponseDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * AWS 비용 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostResponse {
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    private String currency;
    
    private BigDecimal totalCost;
    
    private List<CostByService> costByServices;
    
    private List<DailyCost> dailyCosts;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CostByService {
        private String serviceName;
        private BigDecimal cost;
        private String unit;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyCost {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        
        private BigDecimal cost;
    }
}

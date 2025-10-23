package com.blockcloud.controller;

import com.blockcloud.dto.ResponseDto.CostForecastResponse;
import com.blockcloud.dto.ResponseDto.CostResponse;
import com.blockcloud.enums.AwsService;
import com.blockcloud.service.AwsCostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * AWS 비용 관리 REST API 컨트롤러
 * AWS Cost Explorer API를 사용하여 비용 정보를 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/aws/costs")
@RequiredArgsConstructor
@Tag(name = "AWS Cost Management", description = "AWS 비용 관리 API")
public class AwsCostController {

    private final AwsCostService awsCostService;

    /**
     * 전체 비용 조회 (일별)
     */
    @GetMapping("/total")
    @Operation(summary = "전체 비용 조회", description = "지정된 기간의 AWS 전체 비용을 일별로 조회합니다.")
    public ResponseEntity<CostResponse> getTotalCost(
            @Parameter(description = "시작 날짜 (yyyy-MM-dd)", example = "2025-06-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료 날짜 (yyyy-MM-dd)", example = "2025-10-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Request received - Get total cost: {} to {}", startDate, endDate);
        CostResponse response = awsCostService.getTotalCost(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * 서비스별 비용 조회
     */
    @GetMapping("/by-service")
    @Operation(summary = "서비스별 비용 조회", description = "지정된 기간의 AWS 서비스별 비용을 조회합니다.")
    public ResponseEntity<CostResponse> getCostByService(
            @Parameter(description = "시작 날짜 (yyyy-MM-dd)", example = "2025-06-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료 날짜 (yyyy-MM-dd)", example = "2025-10-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Request received - Get cost by service: {} to {}", startDate, endDate);
        CostResponse response = awsCostService.getCostByService(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 서비스 비용 조회
     */
    @GetMapping("/service/{serviceName}")
    @Operation(summary = "특정 서비스 비용 조회", description = "지정된 기간의 특정 AWS 서비스 비용을 조회합니다.")
    public ResponseEntity<CostResponse> getCostBySpecificService(
            @Parameter(
                description = "AWS 서비스 이름", 
                example = "Amazon Elastic Compute Cloud - Compute",
                schema = @Schema(implementation = AwsService.class)
            )
            @PathVariable String serviceName,
            @Parameter(description = "시작 날짜 (yyyy-MM-dd)", example = "2025-06-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료 날짜 (yyyy-MM-dd)", example = "2025-10-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Request received - Get cost for service '{}': {} to {}", 
                serviceName, startDate, endDate);
        CostResponse response = awsCostService.getCostBySpecificService(serviceName, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * 비용 예측 조회 (최대 3개월, 미래 날짜만)
     * 주의: 예측을 위해서는 최소 2개월 이상의 과거 데이터가 필요합니다.
     */
    @GetMapping("/forecast")
    @Operation(summary = "비용 예측 조회", description = "지정된 기간의 AWS 비용을 예측합니다. (최대 3개월, 미래 날짜만, 최소 2개월 과거 데이터 필요)")
    public ResponseEntity<CostForecastResponse> getCostForecast(
            @Parameter(description = "시작 날짜 (yyyy-MM-dd)", example = "2025-10-23")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료 날짜 (yyyy-MM-dd)", example = "2025-11-23")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Request received - Get cost forecast: {} to {}", startDate, endDate);
        CostForecastResponse response = awsCostService.getCostForecast(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * 월별 비용 조회 (최대 13개월)
     */
    @GetMapping("/monthly")
    @Operation(summary = "월별 비용 조회", description = "지정된 기간의 AWS 월별 비용을 조회합니다. (최대 13개월)")
    public ResponseEntity<CostResponse> getMonthlyCost(
            @Parameter(description = "시작 날짜 (yyyy-MM-dd)", example = "2024-06-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료 날짜 (yyyy-MM-dd)", example = "2025-10-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Request received - Get monthly cost: {} to {}", startDate, endDate);
        CostResponse response = awsCostService.getMonthlyCost(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * 현재 월 비용 조회
     */
    @GetMapping("/current-month")
    @Operation(summary = "현재 월 비용 조회", description = "현재 월의 AWS 비용을 조회합니다.")
    public ResponseEntity<CostResponse> getCurrentMonthCost() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now().plusDays(1); // Cost Explorer는 미래 날짜 하루 포함
        
        log.info("Request received - Get current month cost: {} to {}", startDate, endDate);
        CostResponse response = awsCostService.getTotalCost(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * 지난 30일 비용 조회
     */
    @GetMapping("/last-30-days")
    @Operation(summary = "지난 30일 비용 조회", description = "지난 30일간의 AWS 비용을 조회합니다.")
    public ResponseEntity<CostResponse> getLast30DaysCost() {
        LocalDate endDate = LocalDate.now().plusDays(1);
        LocalDate startDate = endDate.minusDays(30);
        
        log.info("Request received - Get last 30 days cost: {} to {}", startDate, endDate);
        CostResponse response = awsCostService.getTotalCost(startDate, endDate);
        return ResponseEntity.ok(response);
    }
}
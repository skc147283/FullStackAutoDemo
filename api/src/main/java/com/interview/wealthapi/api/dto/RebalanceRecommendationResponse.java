package com.interview.wealthapi.api.dto;

import java.math.BigDecimal;
import java.util.Map;

public record RebalanceRecommendationResponse(
        BigDecimal totalMarketValue,
        Map<String, BigDecimal> currentAllocationPercent,
        Map<String, Integer> targetAllocationPercent,
        String guidance
) {
}

package com.interview.wealthapi;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

final class ApiTestDataFactory {

    private ApiTestDataFactory() {
    }

    static String uniqueEmail(String prefix) {
        return prefix + "." + UUID.randomUUID() + "@example.com";
    }

    static Map<String, Object> customerPayload(String email, String riskProfile) {
        return Map.of(
                "fullName", "Asha Verma",
                "email", email,
                "riskProfile", riskProfile
        );
    }

    static Map<String, Object> accountPayload(String customerId, String currency, BigDecimal openingBalance) {
        return Map.of(
                "customerId", customerId,
                "currency", currency,
                "openingBalance", openingBalance
        );
    }

    static Map<String, Object> depositPayload(BigDecimal amount, String reason) {
        return Map.of(
                "amount", amount,
                "reason", reason
        );
    }

    static Map<String, Object> transferPayload(String sourceAccountId, String destinationAccountId, BigDecimal amount, String clientRequestId) {
        return Map.of(
                "sourceAccountId", sourceAccountId,
                "destinationAccountId", destinationAccountId,
                "amount", amount,
                "clientRequestId", clientRequestId
        );
    }

    static Map<String, Object> holdingPayload(String symbol, BigDecimal marketValue) {
        return Map.of(
                "symbol", symbol,
                "marketValue", marketValue
        );
    }
}
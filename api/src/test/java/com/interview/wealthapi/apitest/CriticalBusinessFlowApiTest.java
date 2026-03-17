package com.interview.wealthapi.apitest;

import com.interview.wealthapi.AbstractRestAssuredApiTest;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

class CriticalBusinessFlowApiTest extends AbstractRestAssuredApiTest {

    @Test
    void shouldExecuteCriticalWealthFlowEndToEnd() {
        String customerId = createCustomer();
        String sourceAccountId = createAccount(customerId, new BigDecimal("1000.00"));
        String destinationAccountId = createAccount(customerId, new BigDecimal("500.00"));

        deposit(sourceAccountId, new BigDecimal("250.00"), "Monthly savings")
                .statusCode(200)
                .body("balance", equalTo(1250.00f));

        transfer(sourceAccountId, destinationAccountId, new BigDecimal("300.00"), randomUuid())
                .statusCode(200)
                .body("status", equalTo("Transfer successful"));

        addHolding(customerId, "EQUITY", new BigDecimal("12000.50"))
                .statusCode(201)
                .body("symbol", equalTo("EQUITY"));

        rebalancePreview(customerId)
                .statusCode(200)
                .body("totalMarketValue", greaterThan(0f));

        getAccount(sourceAccountId)
                .statusCode(200)
                .body("balance", equalTo(950.00f));

        getAccount(destinationAccountId)
                .statusCode(200)
                .body("balance", equalTo(800.00f));

        OffsetDateTime now = OffsetDateTime.now();
        getStatement(sourceAccountId, now.minusDays(2).toString(), now.plusDays(1).toString())
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(2));
    }
}

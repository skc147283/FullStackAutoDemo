package com.interview.wealthapi;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.http.ContentType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PortfolioApiRestAssuredTest extends AbstractRestAssuredApiTest {

    @Test
    void shouldAddHoldingsAndGenerateBalancedRebalancePreview() {
        String customerId = createCustomer(ApiTestDataFactory.uniqueEmail("portfolio"), "BALANCED");

        addHolding(customerId, "EQUITY", BigDecimal.valueOf(12000.50))
                .statusCode(201)
                .body("symbol", equalTo("EQUITY"));

        addHolding(customerId, "DEBT", BigDecimal.valueOf(8000.00))
                .statusCode(201)
                .body("symbol", equalTo("DEBT"));

        rebalancePreview(customerId)
                .statusCode(200)
                .body("totalMarketValue", equalTo(20000.5000F))
                .body("currentAllocationPercent.EQUITY", equalTo(60.00F))
                .body("currentAllocationPercent.DEBT", equalTo(40.00F))
                .body("targetAllocationPercent.EQUITY", equalTo(50))
                .body("targetAllocationPercent.BONDS", equalTo(40))
                .body("targetAllocationPercent.CASH", equalTo(10))
                .body("guidance", containsString("Shift portfolio gradually"));
    }

    @Test
    void shouldReturnNotFoundForUnknownCustomerHoldingRequest() {
        addHolding(randomUuid(), "EQUITY", BigDecimal.valueOf(1000))
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("code", equalTo("NOT_FOUND"))
                .body("message", equalTo("Customer not found"));
    }
}
package com.interview.wealthapi;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractRestAssuredApiTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void configureRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    protected String createCustomer() {
        return createCustomer(ApiTestDataFactory.uniqueEmail("customer"), "BALANCED");
    }

    protected String createCustomer(String email, String riskProfile) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(ApiTestDataFactory.customerPayload(email, riskProfile))
                .when()
                .post("/api/v1/customers")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    protected String createAccount(String customerId, BigDecimal openingBalance) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(ApiTestDataFactory.accountPayload(customerId, "USD", openingBalance))
                .when()
                .post("/api/v1/accounts")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    protected ValidatableResponse deposit(String accountId, BigDecimal amount, String reason) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(ApiTestDataFactory.depositPayload(amount, reason))
                .when()
                .post("/api/v1/accounts/{accountId}/deposit", accountId)
                .then();
    }

    protected ValidatableResponse transfer(String sourceAccountId, String destinationAccountId, BigDecimal amount, String clientRequestId) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(ApiTestDataFactory.transferPayload(sourceAccountId, destinationAccountId, amount, clientRequestId))
                .when()
                .post("/api/v1/accounts/transfer")
                .then();
    }

    protected ValidatableResponse addHolding(String customerId, String symbol, BigDecimal marketValue) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(ApiTestDataFactory.holdingPayload(symbol, marketValue))
                .when()
                .post("/api/v1/portfolios/{customerId}/holdings", customerId)
                .then();
    }

    protected ValidatableResponse getAccount(String accountId) {
        return RestAssured.given()
                .when()
                .get("/api/v1/accounts/{accountId}", accountId)
                .then();
    }

    protected ValidatableResponse getStatement(String accountId, String from, String to) {
        return RestAssured.given()
                .queryParam("from", from)
                .queryParam("to", to)
                .when()
                .get("/api/v1/accounts/{accountId}/statement", accountId)
                .then();
    }

    protected ValidatableResponse rebalancePreview(String customerId) {
        return RestAssured.given()
                .when()
                .post("/api/v1/portfolios/{customerId}/rebalance-preview", customerId)
                .then();
    }

    protected String randomUuid() {
        return UUID.randomUUID().toString();
    }
}
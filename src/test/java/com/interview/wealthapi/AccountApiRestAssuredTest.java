package com.interview.wealthapi;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class AccountApiRestAssuredTest extends AbstractRestAssuredApiTest {

    @Test
    void shouldCompleteHappyPathForBankingFlow() {
        String customerId = createCustomer();
        String sourceAccountId = createAccount(customerId, BigDecimal.valueOf(1000));
        String destinationAccountId = createAccount(customerId, BigDecimal.valueOf(500));

        deposit(sourceAccountId, BigDecimal.valueOf(100), "Monthly savings")
                .statusCode(200)
                .body("balance", equalTo(1100.00F));

        transfer(sourceAccountId, destinationAccountId, BigDecimal.valueOf(250), "trf-happy-path-001")
                .statusCode(200)
                .body("status", equalTo("Transfer successful"));

        getAccount(sourceAccountId)
                .statusCode(200)
                .body("balance", equalTo(850.00F));

        getAccount(destinationAccountId)
                .statusCode(200)
                .body("balance", equalTo(750.00F));

        String from = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1).toString();
        String to = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1).toString();

        getStatement(sourceAccountId, from, to)
                .statusCode(200)
                .body("", hasSize(2))
                .body("[0].direction", equalTo("DEBIT"))
                .body("[1].direction", equalTo("CREDIT"));
    }

    @Test
    void shouldIgnoreDuplicateTransferRequestId() {
        String customerId = createCustomer();
        String sourceAccountId = createAccount(customerId, BigDecimal.valueOf(1000));
        String destinationAccountId = createAccount(customerId, BigDecimal.valueOf(500));

        transfer(sourceAccountId, destinationAccountId, BigDecimal.valueOf(100), "trf-duplicate-001")
                .statusCode(200)
                .body("status", equalTo("Transfer successful"));

        transfer(sourceAccountId, destinationAccountId, BigDecimal.valueOf(100), "trf-duplicate-001")
                .statusCode(200)
                .body("status", containsString("Duplicate request ignored"));

        getAccount(sourceAccountId)
                .statusCode(200)
                .body("balance", equalTo(900.00F));

        getAccount(destinationAccountId)
                .statusCode(200)
                .body("balance", equalTo(600.00F));
    }

    @Test
    void shouldReturnValidationErrorForNegativeDeposit() {
        String customerId = createCustomer();
        String accountId = createAccount(customerId, BigDecimal.valueOf(250));

        deposit(accountId, BigDecimal.valueOf(-10), "Invalid deposit")
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("code", equalTo("VALIDATION_ERROR"))
                .body("message", containsString("amount"));
    }

    @Test
    void shouldReturnBusinessRuleForInsufficientBalance() {
        String customerId = createCustomer();
        String sourceAccountId = createAccount(customerId, BigDecimal.valueOf(50));
        String destinationAccountId = createAccount(customerId, BigDecimal.valueOf(500));

        transfer(sourceAccountId, destinationAccountId, BigDecimal.valueOf(100), "trf-insufficient-001")
                .statusCode(422)
                .contentType(ContentType.JSON)
                .body("code", equalTo("BUSINESS_RULE"))
                .body("message", equalTo("Insufficient balance"));
    }

    @Test
    void shouldReturnNotFoundForUnknownAccount() {
        getAccount(randomUuid())
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("code", equalTo("NOT_FOUND"))
                .body("message", equalTo("Account not found"));
    }
}
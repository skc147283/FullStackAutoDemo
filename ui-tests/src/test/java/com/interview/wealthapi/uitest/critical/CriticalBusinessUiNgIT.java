package com.interview.wealthapi.uitest.critical;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.wealthapi.WealthApiApplication;
import com.interview.wealthapi.uitest.pages.WealthDashboardPage;
import com.interview.wealthapi.uitest.support.UiTestRuntime;
import com.interview.wealthapi.uitest.support.WebDriverFactory;
import io.qameta.allure.testng.AllureTestNg;
import java.util.UUID;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Listeners(AllureTestNg.class)
public class CriticalBusinessUiNgIT {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private WealthDashboardPage page;

    @BeforeClass(alwaysRun = true)
    public void startApp() {
        UiTestRuntime.ensureAppStarted(WealthApiApplication.class);
    }

    @BeforeMethod(alwaysRun = true)
    public void openApp() {
        page = new WealthDashboardPage(WebDriverFactory.getOrCreate());
        page.open(UiTestRuntime.baseUrl());
    }

    @AfterMethod(alwaysRun = true)
    public void closeBrowser() {
        WebDriverFactory.dispose();
    }

    @AfterClass(alwaysRun = true)
    public void stopApp() {
        UiTestRuntime.shutdownApp();
    }

    @DataProvider(name = "portfolioPositiveCases")
    public Object[][] portfolioPositiveCases() {
        return new Object[][]{
                {"CONSERVATIVE", "BONDS", "9000.00", 70, 20, 10},
                {"BALANCED", "EQUITY", "12000.50", 40, 50, 10},
                {"AGGRESSIVE", "ETF", "15000.25", 15, 80, 5}
        };
    }

    @Test(dataProvider = "portfolioPositiveCases")
    public void rebalancePreviewReflectsTargetAllocation(String riskProfile, String symbol, String marketValue,
            int expectedBonds, int expectedEquity, int expectedCash) {
        String customerStatus = createCustomer(riskProfile);
        String customerId = extractField(customerStatus, "id");

        String holdingStatus = page.addHolding(customerId, symbol, marketValue);
        String rebalanceStatus = page.rebalance(customerId);

        Assert.assertTrue(holdingStatus.contains(symbol), "Holding response should include submitted symbol");
        JsonNode rebalanceNode = readJson(rebalanceStatus);
        JsonNode targetAllocation = rebalanceNode.path("targetAllocationPercent");

        Assert.assertTrue(rebalanceNode.hasNonNull("guidance"), "Rebalance response should include guidance");
        Assert.assertEquals(targetAllocation.path("BONDS").asInt(), expectedBonds, "Unexpected BONDS target allocation");
        Assert.assertEquals(targetAllocation.path("EQUITY").asInt(), expectedEquity, "Unexpected EQUITY target allocation");
        Assert.assertEquals(targetAllocation.path("CASH").asInt(), expectedCash, "Unexpected CASH target allocation");
    }

    @DataProvider(name = "transactionNegativeCases")
    public Object[][] transactionNegativeCases() {
        return new Object[][]{
            {"negative-opening-balance", "Amount must be greater than zero", "BUSINESS_RULE"},
                {"zero-deposit", "amount must be greater than 0", "VALIDATION_ERROR"},
                {"insufficient-balance-transfer", "Insufficient balance", "BUSINESS_RULE"},
                {"same-account-transfer", "Source and destination accounts must be different", "BUSINESS_RULE"}
        };
    }

    @Test(dataProvider = "transactionNegativeCases")
    public void criticalBusinessRulesReturnExpectedErrors(String scenarioKey, String expectedMessage,
            String expectedCode) {
        String actualStatus;
        switch (scenarioKey) {
            case "negative-opening-balance" -> {
                String customerId = extractField(createCustomer("BALANCED"), "id");
                actualStatus = page.createAccount(customerId, "-10.00");
            }
            case "zero-deposit" -> {
                AccountPair accounts = createAccounts("BALANCED", "500.00", "200.00");
                actualStatus = page.deposit(accounts.sourceAccountId(), "0", "invalid zero deposit");
            }
            case "insufficient-balance-transfer" -> {
                AccountPair accounts = createAccounts("BALANCED", "100.00", "50.00");
                actualStatus = page.transfer(
                        accounts.sourceAccountId(),
                        accounts.destinationAccountId(),
                        "250.00",
                        "ui-neg-" + UUID.randomUUID());
            }
            case "same-account-transfer" -> {
                AccountPair accounts = createAccounts("BALANCED", "400.00", "100.00");
                actualStatus = page.transfer(
                        accounts.sourceAccountId(),
                        accounts.sourceAccountId(),
                        "25.00",
                        "ui-same-" + UUID.randomUUID());
            }
            default -> throw new IllegalArgumentException("Unsupported scenario key: " + scenarioKey);
        }

        JsonNode errorNode = readJson(actualStatus);
        Assert.assertEquals(errorNode.path("code").asText(), expectedCode, "Unexpected error code");
        Assert.assertEquals(errorNode.path("message").asText(), expectedMessage, "Unexpected error message");
    }

    @DataProvider(name = "idempotentTransferCases")
    public Object[][] idempotentTransferCases() {
        return new Object[][]{
                {"BALANCED", "1000.00", "250.00", "125.00"},
                {"CONSERVATIVE", "800.00", "100.00", "75.00"}
        };
    }

    @Test(dataProvider = "idempotentTransferCases")
    public void duplicateTransferRequestIsHandledIdempotently(String riskProfile, String sourceOpeningBalance,
            String destinationOpeningBalance, String transferAmount) {
        AccountPair accounts = createAccounts(riskProfile, sourceOpeningBalance, destinationOpeningBalance);
        String requestId = "ui-dup-" + UUID.randomUUID();

        String firstTransferStatus = page.transfer(
                accounts.sourceAccountId(),
                accounts.destinationAccountId(),
                transferAmount,
                requestId);
        String secondTransferStatus = page.transfer(
                accounts.sourceAccountId(),
                accounts.destinationAccountId(),
                transferAmount,
                requestId);

        Assert.assertTrue(firstTransferStatus.contains("Transfer successful"), "First transfer should succeed");
        Assert.assertTrue(secondTransferStatus.contains("Duplicate request ignored"),
                "Second transfer should be handled as an idempotent duplicate");
    }

    private String createCustomer(String riskProfile) {
        String email = "ui." + UUID.randomUUID() + "@example.com";
        return page.createCustomer("UI Data Driven User", email, riskProfile);
    }

    private AccountPair createAccounts(String riskProfile, String firstOpeningBalance, String secondOpeningBalance) {
        String customerId = extractField(createCustomer(riskProfile), "id");
        String sourceAccountStatus = page.createAccount(customerId, firstOpeningBalance);
        String destinationAccountStatus = page.createAccount(customerId, secondOpeningBalance);

        return new AccountPair(
                customerId,
                extractField(sourceAccountStatus, "id"),
                extractField(destinationAccountStatus, "id"));
    }

    private String extractField(String json, String fieldName) {
        JsonNode node = readJson(json);
        JsonNode valueNode = node.get(fieldName);
        if (valueNode == null || valueNode.isNull()) {
            throw new IllegalStateException("Could not find field " + fieldName + " in response: " + json);
        }
        return valueNode.asText();
    }

    private JsonNode readJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not parse JSON response: " + json, ex);
        }
    }

    private record AccountPair(String customerId, String sourceAccountId, String destinationAccountId) {
    }
}
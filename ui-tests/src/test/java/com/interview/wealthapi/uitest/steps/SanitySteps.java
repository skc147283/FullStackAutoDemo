package com.interview.wealthapi.uitest.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.interview.wealthapi.uitest.pages.WealthDashboardPage;
import com.interview.wealthapi.uitest.support.UiScenarioContext;
import com.interview.wealthapi.uitest.support.WebDriverFactory;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;

/**
 * Step definitions for @sanity scenarios.
 */
public class SanitySteps extends SharedUiSteps {

    private final WealthDashboardPage page = new WealthDashboardPage(WebDriverFactory.getOrCreate());
    private final UiScenarioContext context = new UiScenarioContext();
    private final String baseUrl = System.getProperty("ui.base-url", "http://localhost:8080");

    /** Latest observable response from the most recent action step. */
    private String lastResponse;

    @Given("I open the app for a sanity test")
    public void openAppForSanity() {
        page.open(baseUrl);
    }

    @When("I onboard a new {string} risk customer")
    public void onboardCustomerWithRisk(String risk) {
        String email = "test.sanity." + System.currentTimeMillis() + "@qa.internal";
        String customerResponse = page.createCustomer("Sanity Test User", email, risk);
        context.setCustomerId(extractField(customerResponse, "id"));
    }

    @When("I open an account with opening balance {int}")
    public void openSingleAccount(int balance) {
        String accountResponse = page.createAccount(context.getCustomerId(), String.valueOf(balance));
        context.setSourceAccountId(extractField(accountResponse, "id"));
    }

    @When("I deposit {int} into the account")
    public void depositIntoAccount(int amount) {
        lastResponse = page.deposit(context.getSourceAccountId(), String.valueOf(amount), "Sanity deposit");
    }

    @Then("the deposit response should show balance {double}")
    public void depositResponseShowsBalance(double expectedBalance) {
        Assertions.assertTrue(lastResponse.contains("\"balance\""),
                "Deposit response should contain 'balance': " + lastResponse);
        try {
            JsonNode node = objectMapper.readTree(lastResponse);
            double actual = node.get("balance").asDouble();
            Assertions.assertEquals(expectedBalance, actual, 0.01,
                    "Expected balance " + expectedBalance + " but got " + actual);
        } catch (Exception e) {
            Assertions.fail("Could not parse deposit response: " + lastResponse);
        }
    }

    @When("I add a holding with symbol {string} and market value {double}")
    public void addHoldingWithSymbol(String symbol, double marketValue) {
        lastResponse = page.addHolding(context.getCustomerId(), symbol, String.valueOf(marketValue));
    }

    @Then("the holding response should contain symbol {string}")
    public void holdingResponseContainsSymbol(String symbol) {
        Assertions.assertTrue(lastResponse.contains(symbol),
                "Holding response should contain '" + symbol + "': " + lastResponse);
    }

    @When("I create two accounts with balances {int} and {int}")
    public void createTwoAccounts(int balance1, int balance2) {
        String sourceResponse = page.createAccount(context.getCustomerId(), String.valueOf(balance1));
        context.setSourceAccountId(extractField(sourceResponse, "id"));

        String destResponse = page.createAccount(context.getCustomerId(), String.valueOf(balance2));
        context.setDestinationAccountId(extractField(destResponse, "id"));
    }

    @When("I transfer {int} between the accounts")
    public void transferBetweenAccounts(int amount) {
        lastResponse = page.transfer(
                context.getSourceAccountId(),
                context.getDestinationAccountId(),
                String.valueOf(amount),
                "sanity-trf-" + UUID.randomUUID()
        );
    }

    @Then("the transfer response should indicate success")
    public void transferResponseIndicatesSuccess() {
        Assertions.assertTrue(lastResponse.contains("Transfer successful"),
                "Transfer response should say 'Transfer successful': " + lastResponse);
    }
}

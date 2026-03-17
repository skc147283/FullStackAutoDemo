package com.interview.wealthapi.uitest.steps;

import com.interview.wealthapi.uitest.pages.WealthDashboardPage;
import com.interview.wealthapi.uitest.support.UiScenarioContext;
import com.interview.wealthapi.uitest.support.WebDriverFactory;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;

public class CriticalWealthFlowSteps extends SharedUiSteps {

    private final UiScenarioContext context = new UiScenarioContext();
    private final WealthDashboardPage page = new WealthDashboardPage(WebDriverFactory.getOrCreate());
    private final String baseUrl = System.getProperty("ui.base-url", "http://localhost:8080");

    private String customerStatus;
    private String sourceAccountStatus;
    private String destinationAccountStatus;
    private String depositStatus;
    private String transferStatus;
    private String holdingStatus;
    private String rebalanceStatus;

    @Given("I open the wealth UI app")
    public void openUiApp() {
        page.open(baseUrl);
    }

    @When("I onboard a balanced customer")
    public void onboardCustomer() {
        String email = "test.ui." + System.currentTimeMillis() + "@qa.internal";
        customerStatus = page.createCustomer("UI Demo User", email, "BALANCED");
        context.setCustomerId(extractField(customerStatus, "id"));
    }

    @When("I open two USD accounts with opening balances {int} and {int}")
    public void openTwoAccounts(int firstAmount, int secondAmount) {
        sourceAccountStatus = page.createAccount(context.getCustomerId(), String.valueOf(firstAmount));
        context.setSourceAccountId(extractField(sourceAccountStatus, "id"));

        destinationAccountStatus = page.createAccount(context.getCustomerId(), String.valueOf(secondAmount));
        context.setDestinationAccountId(extractField(destinationAccountStatus, "id"));
    }

    @When("I deposit {int} into the source account")
    public void depositIntoSource(int amount) {
        depositStatus = page.deposit(context.getSourceAccountId(), String.valueOf(amount), "UI savings deposit");
    }

    @When("I transfer {int} from source to destination account")
    public void transferFunds(int amount) {
        transferStatus = page.transfer(
                context.getSourceAccountId(),
                context.getDestinationAccountId(),
                String.valueOf(amount),
                "ui-trf-" + UUID.randomUUID()
        );
    }

    @When("I add EQUITY holding of {double}")
    public void addHolding(double value) {
        holdingStatus = page.addHolding(context.getCustomerId(), "EQUITY", String.valueOf(value));
    }

    @When("I request rebalance preview")
    public void requestRebalance() {
        rebalanceStatus = page.rebalance(context.getCustomerId());
    }

    @Then("each workflow step should show a success response")
    public void verifySuccesses() {
        Assertions.assertTrue(customerStatus.contains("\"id\""), "Customer response should include id");
        Assertions.assertTrue(sourceAccountStatus.contains("\"id\""), "Source account response should include id");
        Assertions.assertTrue(destinationAccountStatus.contains("\"id\""), "Destination account response should include id");
        Assertions.assertTrue(depositStatus.contains("\"balance\""), "Deposit response should include balance");
        Assertions.assertTrue(transferStatus.contains("Transfer successful"), "Transfer response should indicate success");
        Assertions.assertTrue(holdingStatus.contains("EQUITY"), "Holding response should include EQUITY");
        Assertions.assertTrue(rebalanceStatus.contains("guidance"), "Rebalance response should include guidance");
    }

}


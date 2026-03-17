package com.interview.wealthapi.uitest.steps;

import com.interview.wealthapi.uitest.pages.WealthDashboardPage;
import com.interview.wealthapi.uitest.support.WebDriverFactory;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;

/**
 * Step definitions for @smoke scenarios.
 */
public class SmokeSteps extends SharedUiSteps {

    private final WealthDashboardPage page = new WealthDashboardPage(WebDriverFactory.getOrCreate());
    private final String baseUrl = System.getProperty("ui.base-url", "http://localhost:8080");

    /** Latest observable response from the most recent action step. */
    private String lastResponse;

    @Given("I navigate to the wealth management UI")
    public void navigateToUi() {
        page.open(baseUrl);
    }

    @Then("the page title should contain {string}")
    public void pageTitleContains(String expected) {
        String title = page.getPageTitle();
        Assertions.assertTrue(title.contains(expected),
                "Page title should contain '" + expected + "' but was: " + title);
    }

    @Then("the customer creation form should be visible")
    public void customerFormVisible() {
        Assertions.assertTrue(page.isElementVisible(By.id("customerName")),
                "customerName field should be visible");
        Assertions.assertTrue(page.isElementVisible(By.id("createCustomerBtn")),
                "createCustomerBtn should be visible");
    }

    @When("I submit a new customer with name {string} and risk {string}")
    public void submitNewCustomer(String name, String risk) {
        String email = "test.smoke." + System.currentTimeMillis() + "@qa.internal";
        lastResponse = page.createCustomer(name, email, risk);
    }

    @Then("the customer response should contain a valid ID")
    public void customerResponseHasId() {
        Assertions.assertTrue(lastResponse.contains("\"id\""),
                "Customer response should contain 'id' field: " + lastResponse);
    }
}

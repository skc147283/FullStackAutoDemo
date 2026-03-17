package com.interview.wealthapi.uitest.steps;

import com.interview.wealthapi.uitest.pages.AdvancedDashboardPage;
import com.interview.wealthapi.uitest.support.WebDriverFactory;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.File;
import java.net.URL;
import org.junit.jupiter.api.Assertions;

public class AdvancedDashboardSteps {

    private final AdvancedDashboardPage page = new AdvancedDashboardPage(WebDriverFactory.getOrCreate());
    private final String baseUrl = System.getProperty("ui.base-url", "http://localhost:8080");

    private String lastAlert;
    private boolean fileInfoShown;

    @Given("I open the advanced dashboard")
    public void openAdvancedDashboard() {
        page.open(baseUrl);
    }

    @When("I switch to advanced tab {string} with id {string}")
    public void switchToAdvancedTab(String tabLabel, String tabId) {
        page.openTab(tabId, tabLabel);
    }

    @Then("advanced tab with id {string} should be active")
    public void verifyAdvancedTabActive(String tabId) {
        Assertions.assertTrue(page.isTabActive(tabId), "Expected tab to be active: " + tabId);
    }

    @When("I generate advanced report type {string} for range {string}")
    public void generateAdvancedReport(String reportType, String dateRange) {
        lastAlert = page.generateReport(reportType, dateRange);
    }

    @Then("advanced report success alert should be displayed")
    public void verifyAdvancedReportAlert() {
        Assertions.assertTrue(lastAlert.contains("Report generated successfully"),
                "Expected report success alert but got: " + lastAlert);
    }

    @When("I upload advanced csv fixture {string}")
    public void uploadAdvancedCsvFixture(String classpathFixturePath) {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(classpathFixturePath);
        Assertions.assertNotNull(resource, "Fixture not found on classpath: " + classpathFixturePath);
        File file = new File(resource.getFile());
        fileInfoShown = page.uploadCsv(file.getAbsolutePath());
        lastAlert = page.latestAlertText();
    }

    @Then("advanced upload should show file details and success")
    public void verifyAdvancedUpload() {
        Assertions.assertTrue(fileInfoShown, "File info block should be visible after selecting file");
        Assertions.assertTrue(page.uploadProgressVisible(), "Upload progress should be visible");
        Assertions.assertTrue(lastAlert.contains("File uploaded successfully"),
                "Expected upload success alert but got: " + lastAlert);
    }

    @When("I perform quick deposit of {string} from advanced dashboard")
    public void quickDepositFromAdvancedDashboard(String amount) {
        lastAlert = page.quickDeposit(amount);
    }

    @Then("advanced deposit success alert should be displayed")
    public void verifyAdvancedDepositAlert() {
        Assertions.assertTrue(lastAlert.contains("Deposit of"),
                "Expected deposit alert to contain amount but got: " + lastAlert);
    }
}

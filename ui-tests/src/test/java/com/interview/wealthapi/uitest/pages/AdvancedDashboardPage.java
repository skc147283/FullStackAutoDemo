package com.interview.wealthapi.uitest.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AdvancedDashboardPage extends BasePage {

    private static final By ALERT_CONTAINER = By.id("alertContainer");
    private static final By LATEST_ALERT = By.cssSelector("#alertContainer .alert:last-child");

    private static final By REPORT_TYPE = By.id("reportType");
    private static final By DATE_RANGE = By.id("dateRange");
    private static final By GENERATE_REPORT_BTN = By.cssSelector("#reports button[type='submit']");

    private static final By FILE_INPUT = By.id("csvFile");
    private static final By FILE_INFO = By.id("fileInfo");
    private static final By UPLOAD_BTN = By.id("uploadBtn");
    private static final By PROGRESS_BAR = By.id("progressBar");

    private static final By DEPOSIT_QUICK_ACTION = By.xpath("//button[contains(., 'Deposit Funds')]");
    private static final By DEPOSIT_MODAL_AMOUNT = By.cssSelector("#depositModal #depositAmount");
    private static final By DEPOSIT_MODAL_SUBMIT = By.cssSelector("#depositModal button[type='submit']");

    public AdvancedDashboardPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/dashboard.html");
    }

    public void openTab(String tabId, String tabLabel) {
        click(By.xpath("//button[contains(@class,'tab-button') and contains(., '" + tabLabel + "')]") );
        waitVisibleFluent(By.id(tabId));
    }

    public boolean isTabActive(String tabId) {
        String classes = waitVisibleFluent(By.id(tabId)).getAttribute("class");
        return classes != null && classes.contains("active");
    }

    public String generateReport(String reportType, String dateRange) {
        openTab("reports", "Reports");
        selectByValueOrVisibleText(REPORT_TYPE, reportType);
        selectByValueOrVisibleText(DATE_RANGE, dateRange);
        click(GENERATE_REPORT_BTN);
        return waitForLatestAlertText();
    }

    public boolean uploadCsv(String absoluteFilePath) {
        openTab("fileupload", "Upload Data");
        waitPresentFluent(FILE_INPUT).sendKeys(absoluteFilePath);
        boolean infoVisible = isElementVisible(FILE_INFO);
        click(UPLOAD_BTN);
        waitForLatestAlertText();
        return infoVisible;
    }

    public String latestAlertText() {
        return waitForLatestAlertText();
    }

    public boolean uploadProgressVisible() {
        return isElementVisible(PROGRESS_BAR);
    }

    public String quickDeposit(String amount) {
        openTab("overview", "Overview");
        click(DEPOSIT_QUICK_ACTION);
        type(DEPOSIT_MODAL_AMOUNT, amount);
        click(DEPOSIT_MODAL_SUBMIT);
        return waitForLatestAlertText();
    }

    private String waitForLatestAlertText() {
        new org.openqa.selenium.support.ui.FluentWait<>(driver)
                .withTimeout(java.time.Duration.ofSeconds(12))
                .pollingEvery(java.time.Duration.ofMillis(200))
                .ignoring(org.openqa.selenium.NoSuchElementException.class)
                .until(d -> !d.findElements(LATEST_ALERT).isEmpty());
        return driver.findElement(LATEST_ALERT).getText();
    }
}

package com.interview.wealthapi.uitest.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class WealthDashboardPage extends BasePage {

    private static final By CUSTOMER_NAME = By.id("customerName");
    private static final By CUSTOMER_EMAIL = By.id("customerEmail");
    private static final By CUSTOMER_RISK = By.id("customerRisk");
    private static final By CREATE_CUSTOMER = By.id("createCustomerBtn");
    private static final By CUSTOMER_STATUS = By.id("customerStatus");

    private static final By ACCOUNT_CUSTOMER_ID = By.id("accountCustomerId");
    private static final By ACCOUNT_BALANCE = By.id("accountOpeningBalance");
    private static final By CREATE_ACCOUNT = By.id("createAccountBtn");
    private static final By ACCOUNT_STATUS = By.id("accountStatus");

    private static final By DEPOSIT_ACCOUNT_ID = By.id("depositAccountId");
    private static final By DEPOSIT_AMOUNT = By.id("depositAmount");
    private static final By DEPOSIT_REASON = By.id("depositReason");
    private static final By DEPOSIT_BTN = By.id("depositBtn");
    private static final By DEPOSIT_STATUS = By.id("depositStatus");

    private static final By TRANSFER_SOURCE = By.id("transferSourceAccountId");
    private static final By TRANSFER_DEST = By.id("transferDestinationAccountId");
    private static final By TRANSFER_AMOUNT = By.id("transferAmount");
    private static final By TRANSFER_REQUEST = By.id("transferClientRequestId");
    private static final By TRANSFER_BTN = By.id("transferBtn");
    private static final By TRANSFER_STATUS = By.id("transferStatus");

    private static final By HOLDING_CUSTOMER_ID = By.id("holdingCustomerId");
    private static final By HOLDING_SYMBOL = By.id("holdingSymbol");
    private static final By HOLDING_VALUE = By.id("holdingMarketValue");
    private static final By HOLDING_BTN = By.id("holdingBtn");
    private static final By HOLDING_STATUS = By.id("holdingStatus");

    private static final By REBALANCE_CUSTOMER_ID = By.id("rebalanceCustomerId");
    private static final By REBALANCE_BTN = By.id("rebalanceBtn");
    private static final By REBALANCE_STATUS = By.id("rebalanceStatus");

    public WealthDashboardPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/index.html");
    }

    public String createCustomer(String name, String email, String risk) {
        type(CUSTOMER_NAME, name);
        type(CUSTOMER_EMAIL, email);
        selectByValue(CUSTOMER_RISK, risk);
        click(CREATE_CUSTOMER);
        return nonEmptyText(CUSTOMER_STATUS);
    }

    public String createAccount(String customerId, String openingBalance) {
        type(ACCOUNT_CUSTOMER_ID, customerId);
        type(ACCOUNT_BALANCE, openingBalance);
        click(CREATE_ACCOUNT);
        return nonEmptyText(ACCOUNT_STATUS);
    }

    public String deposit(String accountId, String amount, String reason) {
        type(DEPOSIT_ACCOUNT_ID, accountId);
        type(DEPOSIT_AMOUNT, amount);
        type(DEPOSIT_REASON, reason);
        click(DEPOSIT_BTN);
        return nonEmptyText(DEPOSIT_STATUS);
    }

    public String transfer(String source, String destination, String amount, String requestId) {
        type(TRANSFER_SOURCE, source);
        type(TRANSFER_DEST, destination);
        type(TRANSFER_AMOUNT, amount);
        type(TRANSFER_REQUEST, requestId);
        click(TRANSFER_BTN);
        return nonEmptyText(TRANSFER_STATUS);
    }

    public String addHolding(String customerId, String symbol, String marketValue) {
        type(HOLDING_CUSTOMER_ID, customerId);
        type(HOLDING_SYMBOL, symbol);
        type(HOLDING_VALUE, marketValue);
        click(HOLDING_BTN);
        return nonEmptyText(HOLDING_STATUS);
    }

    public String rebalance(String customerId) {
        type(REBALANCE_CUSTOMER_ID, customerId);
        click(REBALANCE_BTN);
        return nonEmptyText(REBALANCE_STATUS);
    }
}

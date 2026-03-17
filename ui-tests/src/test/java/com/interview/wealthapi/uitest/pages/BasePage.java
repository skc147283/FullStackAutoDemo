package com.interview.wealthapi.uitest.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    protected WebElement waitVisibleFluent(By locator) {
        return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(12))
                .pollingEvery(Duration.ofMillis(200))
                .ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitClickableFluent(By locator) {
        return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(12))
                .pollingEvery(Duration.ofMillis(200))
                .ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected WebElement waitPresentFluent(By locator) {
        return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(12))
                .pollingEvery(Duration.ofMillis(200))
                .ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    protected void type(By locator, String value) {
        WebElement input = waitVisibleFluent(locator);
        input.clear();
        input.sendKeys(value);
    }

    protected void click(By locator) {
        waitClickableFluent(locator).click();
    }

    protected void selectByValue(By locator, String value) {
        WebElement selectElement = waitVisibleFluent(locator);
        new Select(selectElement).selectByValue(value);
    }

    protected void selectByValueOrVisibleText(By locator, String option) {
        WebElement selectElement = waitVisibleFluent(locator);
        Select select = new Select(selectElement);
        try {
            select.selectByValue(option);
            return;
        } catch (NoSuchElementException ignored) {
            // Fall back to visible text matching.
        }
        select.selectByVisibleText(option);
    }

    protected String text(By locator) {
        return waitVisibleFluent(locator).getText();
    }

    protected String nonEmptyText(By locator) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        wait.until(driver -> !element.getText().isBlank());
        String rawPayload = element.getAttribute("data-raw-payload");
        if (rawPayload != null && !rawPayload.isBlank()) {
            return rawPayload;
        }
        return element.getText();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public boolean isElementVisible(By locator) {
        try {
            return waitVisibleFluent(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean waitForTextContains(By locator, String token) {
        try {
            return new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(12))
                    .pollingEvery(Duration.ofMillis(200))
                    .ignoring(NoSuchElementException.class)
                    .until(d -> {
                        String value = d.findElement(locator).getText();
                        return value != null && value.contains(token);
                    });
        } catch (Exception e) {
            return false;
        }
    }
}

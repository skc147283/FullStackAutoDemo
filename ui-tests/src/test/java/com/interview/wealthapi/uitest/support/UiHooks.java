package com.interview.wealthapi.uitest.support;

import com.interview.wealthapi.WealthApiApplication;
import io.cucumber.java.AfterAll;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.stream.Collectors;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

public class UiHooks {

    @Before
    public void setUp() {
        UiTestRuntime.ensureAppStarted(WealthApiApplication.class);
        WebDriverFactory.getOrCreate();
    }

    @After
    public void tearDown(Scenario scenario) {
        WebDriver driver = WebDriverFactory.getCurrent();
        if (scenario.isFailed() && driver != null) {
            attachFailureArtifacts(scenario, driver);
        }
        WebDriverFactory.dispose();
    }

    private void attachFailureArtifacts(Scenario scenario, WebDriver driver) {
        if (driver instanceof TakesScreenshot screenshotDriver) {
            byte[] screenshot = screenshotDriver.getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", "failure-screenshot");
        }

        try {
            scenario.attach(driver.getCurrentUrl(), "text/plain", "failure-url");
        } catch (Exception ignored) {
            scenario.attach("Could not capture URL", "text/plain", "failure-url");
        }

        try {
            scenario.attach(driver.getPageSource(), "text/html", "failure-page-source");
        } catch (Exception ignored) {
            scenario.attach("Could not capture page source", "text/plain", "failure-page-source");
        }

        try {
            String browserErrors = driver.manage().logs().get(LogType.BROWSER).getAll().stream()
                    .filter(entry -> "SEVERE".equalsIgnoreCase(entry.getLevel().getName())
                            || "ERROR".equalsIgnoreCase(entry.getLevel().getName()))
                    .map(this::formatLogEntry)
                    .collect(Collectors.joining("\n\n"));
            if (browserErrors.isBlank()) {
                browserErrors = "No browser SEVERE/ERROR console logs captured for this failure.";
            }
            scenario.attach(browserErrors, "text/plain", "failure-browser-console-errors");
        } catch (Exception e) {
            scenario.attach(renderStackTrace(e), "text/plain", "failure-browser-console-errors-capture-failed");
        }
    }

    private String formatLogEntry(LogEntry entry) {
        return "[" + entry.getLevel() + "] " + entry.getMessage();
    }

    private String renderStackTrace(Throwable throwable) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    @AfterAll
    public static void shutdownApp() {
        UiTestRuntime.shutdownApp();
    }
}

package com.interview.wealthapi.uitest.support;

import com.interview.wealthapi.WealthApiApplication;
import io.cucumber.java.AfterAll;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class UiHooks {

    @Before
    public void setUp() {
        UiTestRuntime.ensureAppStarted(WealthApiApplication.class);
        WebDriverFactory.getOrCreate();
    }

    @After
    public void tearDown(Scenario scenario) {
        WebDriver driver = WebDriverFactory.getOrCreate();
        if (scenario.isFailed() && driver instanceof TakesScreenshot screenshotDriver) {
            byte[] screenshot = screenshotDriver.getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", "failure-screenshot");
        }
        WebDriverFactory.dispose();
    }

    @AfterAll
    public static void shutdownApp() {
        UiTestRuntime.shutdownApp();
    }
}

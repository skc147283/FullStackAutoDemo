package com.interview.wealthapi.uitest.support;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public final class WebDriverFactory {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private WebDriverFactory() {
    }

    public static WebDriver getOrCreate() {
        if (DRIVER.get() == null) {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            if (isHeadlessEnabled()) {
                options.addArguments("--headless=new", "--disable-gpu");
            }
            options.addArguments("--window-size=1440,1080", "--no-sandbox", "--disable-dev-shm-usage");
            ChromeDriver driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
            DRIVER.set(driver);
        }
        return DRIVER.get();
    }

    public static WebDriver getCurrent() {
        return DRIVER.get();
    }

    public static void dispose() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            DRIVER.remove();
        }
    }

    private static boolean isHeadlessEnabled() {
        String configuredValue = System.getProperty("ui.headless");
        if (configuredValue != null) {
            return Boolean.parseBoolean(configuredValue);
        }
        return Boolean.parseBoolean(System.getenv().getOrDefault("CI", "false"));
    }
}

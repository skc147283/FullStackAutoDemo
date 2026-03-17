package com.interview.wealthapi.uitest.support;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public final class WebDriverFactory {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private WebDriverFactory() {
    }

    public static WebDriver getOrCreate() {
        if (DRIVER.get() == null) {
            ChromeOptions options = new ChromeOptions();
            configureLinuxCiBrowser(options);

            if (System.getProperty("webdriver.chrome.driver") == null) {
                WebDriverManager.chromedriver().setup();
            }

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

    private static void configureLinuxCiBrowser(ChromeOptions options) {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (!os.contains("linux")) {
            return;
        }

        if (isExecutable("/usr/bin/chromedriver")) {
            System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
        }

        String[] browserCandidates = {
                "/usr/bin/chromium",
                "/usr/bin/chromium-browser",
                "/usr/bin/google-chrome",
                "/usr/bin/google-chrome-stable"
        };
        for (String candidate : browserCandidates) {
            if (isExecutable(candidate)) {
                options.setBinary(candidate);
                break;
            }
        }
    }

    private static boolean isExecutable(String path) {
        return Files.isExecutable(Path.of(path));
    }
}

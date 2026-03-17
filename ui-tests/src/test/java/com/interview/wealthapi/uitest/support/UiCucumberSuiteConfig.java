package com.interview.wealthapi.uitest.support;

/**
 * Shared constants for Cucumber suite runners to avoid duplicated annotation values.
 */
public final class UiCucumberSuiteConfig {

    public static final String GLUE = "com.interview.wealthapi.uitest";
    public static final String ALLURE_PLUGIN = "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm";
    public static final String REPORT_PREFIX = "pretty, summary, html:target/cucumber-reports/";
    public static final String REPORT_SUFFIX = ".html, " + ALLURE_PLUGIN;

    private UiCucumberSuiteConfig() {
    }
}

package com.interview.wealthapi.uitest.smoke;

import com.interview.wealthapi.uitest.support.UiCucumberSuiteConfig;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Smoke suite – fast, lightweight checks that the app boots and basic endpoints respond.
 * Run: mvn verify -pl ui-tests   (this runner is included automatically)
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/smoke")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = UiCucumberSuiteConfig.GLUE)
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@smoke")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = UiCucumberSuiteConfig.REPORT_PREFIX + "ui-smoke" + UiCucumberSuiteConfig.REPORT_SUFFIX)
public class SmokeUiIT {
}

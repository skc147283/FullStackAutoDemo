package com.interview.wealthapi.uitest.critical;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Critical suite – full end-to-end journey: onboard customer, open accounts,
 * deposit, transfer, add holding, rebalance preview.
 * Run: mvn verify -pl ui-tests   (this runner is included automatically)
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/critical")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.interview.wealthapi.uitest")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@critical")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty, summary, html:target/cucumber-reports/ui-critical.html, io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm")
public class CriticalUiIT {
}

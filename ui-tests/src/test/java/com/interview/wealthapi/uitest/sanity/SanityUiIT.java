package com.interview.wealthapi.uitest.sanity;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Sanity suite – per-feature correctness checks (deposit balance, holding symbol, transfer status).
 * Run: mvn verify -pl ui-tests   (this runner is included automatically)
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/sanity")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.interview.wealthapi.uitest")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@sanity")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty, summary, html:target/cucumber-reports/ui-sanity.html, io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm")
public class SanityUiIT {
}

package com.interview.wealthapi.uitest.support;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public final class UiTestRuntime {

    private static ConfigurableApplicationContext appContext;

    private UiTestRuntime() {
    }

    public static synchronized void ensureAppStarted(Class<?> applicationClass) {
        if (appContext == null) {
            appContext = SpringApplication.run(applicationClass, "--server.port=0");
            String port = appContext.getEnvironment().getProperty("local.server.port", "8080");
            System.setProperty("ui.base-url", "http://localhost:" + port);
        }
    }

    public static String baseUrl() {
        return System.getProperty("ui.base-url", "http://localhost:8080");
    }

    public static synchronized void shutdownApp() {
        if (appContext != null) {
            appContext.close();
            appContext = null;
        }
    }
}
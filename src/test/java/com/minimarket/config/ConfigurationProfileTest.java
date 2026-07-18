package com.minimarket.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConfigurationProfileTest {

    @Test
    void productionDefaultsDoNotEnableH2OrDemoSeedCredentials() throws IOException {
        Properties production = load("application.properties");
        Properties test = loadTestProperties();

        assertEquals("false", production.getProperty("spring.h2.console.enabled"));
        assertEquals("false", production.getProperty("app.seed.enabled"));
        assertNull(production.getProperty("spring.datasource.password"));
        assertNull(production.getProperty("app.admin.password"));
        assertNull(production.getProperty("app.cajero.password"));
        assertNull(production.getProperty("app.cliente.password"));
        assertEquals("dev", DemoSeedConfig.class.getAnnotation(Profile.class).value()[0]);
        assertEquals("false", test.getProperty("app.seed.enabled"));
    }

    @Test
    void developmentProfileContainsDemoOnlyConfiguration() throws IOException {
        Properties development = load("application-dev.properties");

        assertEquals("true", development.getProperty("spring.h2.console.enabled"));
        assertEquals("true", development.getProperty("app.seed.enabled"));
        assertEquals("${APP_ADMIN_PASSWORD:local-admin-demo-2026}", development.getProperty("app.admin.password"));
        assertFalse(development.getProperty("spring.datasource.url").isBlank());
    }

    private Properties load(String resource) throws IOException {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(Path.of("src", "main", "resources", resource))) {
            properties.load(input);
        }
        return properties;
    }

    private Properties loadTestProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(Path.of("src", "test", "resources", "application.properties"))) {
            properties.load(input);
        }
        return properties;
    }
}

package com.govos.infrastructure.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

/**
 * Loads {@code application.yml} from the infrastructure module into the Spring Environment.
 * <p>
 * Library JAR configuration files are not loaded by Spring Boot by default.
 */
public class GovosInfrastructureEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "govosInfrastructureConfig";
    private static final String CONFIG_RESOURCE = "govos-infrastructure.yml";

    private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Resource resource = new ClassPathResource(
                CONFIG_RESOURCE,
                GovosInfrastructureEnvironmentPostProcessor.class.getClassLoader());
        if (!resource.exists()) {
            return;
        }
        try {
            List<PropertySource<?>> propertySources = loader.load(PROPERTY_SOURCE_NAME, resource);
            propertySources.forEach(environment.getPropertySources()::addFirst);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load GovOS infrastructure configuration", ex);
        }
    }

}

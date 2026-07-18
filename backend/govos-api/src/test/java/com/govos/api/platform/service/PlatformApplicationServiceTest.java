package com.govos.api.platform.service;

import com.govos.api.platform.config.PlatformProperties;
import com.govos.api.platform.mapper.PlatformMapper;
import com.govos.api.platform.response.PlatformInfoResponse;
import com.govos.api.platform.response.PlatformVersionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.mock.env.MockEnvironment;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformApplicationServiceTest {

    @Mock
    private PlatformMapper platformMapper;

    @Mock
    private HealthEndpoint healthEndpoint;

    @Mock
    private ObjectProvider<BuildProperties> buildPropertiesProvider;

    @Mock
    private ObjectProvider<GitProperties> gitPropertiesProvider;

    @Mock
    private ObjectProvider<org.flywaydb.core.Flyway> flywayProvider;

    @Mock
    private ObjectProvider<javax.sql.DataSource> dataSourceProvider;

    private PlatformProperties platformProperties;
    private MockEnvironment environment;
    private PlatformApplicationService platformApplicationService;

    @BeforeEach
    void setUp() {
        environment = new MockEnvironment()
                .withProperty("spring.application.name", "govos-api")
                .withProperty("info.app.version", "0.1.0-SNAPSHOT");

        platformProperties = new PlatformProperties();
        platformApplicationService = new PlatformApplicationService(
                environment,
                platformProperties,
                platformMapper,
                healthEndpoint,
                buildPropertiesProvider,
                gitPropertiesProvider,
                flywayProvider,
                dataSourceProvider);
    }

    @Test
    void shouldDelegatePlatformInfoMapping() {
        Properties properties = new Properties();
        properties.setProperty("group", "com.govos");
        properties.setProperty("artifact", "govos-api");
        properties.setProperty("name", "govos-api");
        properties.setProperty("version", "0.1.0-SNAPSHOT");
        BuildProperties buildProperties = new BuildProperties(properties);

        when(buildPropertiesProvider.getIfAvailable()).thenReturn(buildProperties);
        when(gitPropertiesProvider.getIfAvailable()).thenReturn(null);
        when(flywayProvider.getIfAvailable()).thenReturn(null);
        when(dataSourceProvider.getIfAvailable()).thenReturn(null);

        PlatformInfoResponse expected = new PlatformInfoResponse(
                "govos-api",
                "0.1.0-SNAPSHOT",
                "default",
                "21",
                "3.5.16",
                "unknown",
                "unknown",
                "unknown",
                "unknown");

        when(platformMapper.toPlatformInfo(
                environment,
                buildProperties,
                null,
                "unknown",
                "unknown")).thenReturn(expected);

        assertThat(platformApplicationService.getPlatformInfo()).isEqualTo(expected);
    }

    @Test
    void shouldDelegatePlatformVersionMapping() {
        Properties properties = new Properties();
        properties.setProperty("group", "com.govos");
        properties.setProperty("artifact", "govos-api");
        properties.setProperty("name", "govos-api");
        properties.setProperty("version", "0.1.0-SNAPSHOT");
        BuildProperties buildProperties = new BuildProperties(properties);

        when(buildPropertiesProvider.getIfAvailable()).thenReturn(buildProperties);

        PlatformVersionResponse expected = new PlatformVersionResponse(
                "0.1.0-SNAPSHOT",
                "0.1.0",
                "2026-07-17");

        when(platformMapper.toPlatformVersion(environment, buildProperties, platformProperties))
                .thenReturn(expected);

        assertThat(platformApplicationService.getPlatformVersion()).isEqualTo(expected);
    }

    @Test
    void shouldDelegateHealthMapping() {
        HealthComponent healthComponent = Health.up().build();

        when(healthEndpoint.health()).thenReturn(healthComponent);
        when(platformMapper.toHealthResponse(healthComponent)).thenReturn(
                new com.govos.api.platform.response.HealthResponse(
                        "UP", "NOT_CONFIGURED", "NOT_CONFIGURED", "NOT_CONFIGURED",
                        "UP", "UP", "10s"));

        assertThat(platformApplicationService.getHealth().database()).isEqualTo("UP");
    }
}

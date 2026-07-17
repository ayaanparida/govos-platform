package com.govos.api.platform.mapper;

import com.govos.api.platform.config.PlatformProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.info.BuildProperties;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformMapperTest {

    @Mock
    private CompositeHealth compositeHealth;

    private PlatformMapper platformMapper;
    private MockEnvironment environment;

    @BeforeEach
    void setUp() {
        platformMapper = new PlatformMapper();
        environment = new MockEnvironment()
                .withProperty("spring.application.name", "govos-api")
                .withProperty("info.app.version", "0.1.0-SNAPSHOT")
                .withProperty("spring.profiles.active", "local");
    }

    @Test
    void shouldMapPlatformInfo() {
        Properties properties = new Properties();
        properties.setProperty("group", "com.govos");
        properties.setProperty("artifact", "govos-api");
        properties.setProperty("name", "govos-api");
        properties.setProperty("version", "0.1.0-SNAPSHOT");
        BuildProperties buildProperties = new BuildProperties(properties);

        var response = platformMapper.toPlatformInfo(
                environment,
                buildProperties,
                null,
                "PostgreSQL",
                "1.7.0");

        assertThat(response.applicationName()).isEqualTo("govos-api");
        assertThat(response.version()).isEqualTo("0.1.0-SNAPSHOT");
        assertThat(response.environment()).isEqualTo("local");
        assertThat(response.database()).isEqualTo("PostgreSQL");
        assertThat(response.flywayVersion()).isEqualTo("1.7.0");
    }

    @Test
    void shouldMapModulesFromConfiguration() {
        PlatformProperties platformProperties = new PlatformProperties();
        platformProperties.setModules(List.of(
                new PlatformProperties.ModuleDefinition("govos-api", "0.1.0-SNAPSHOT", "ACTIVE")));

        List<com.govos.api.platform.response.ModuleResponse> modules =
                platformMapper.toModuleResponses(platformProperties);

        assertThat(modules).hasSize(1);
        assertThat(modules.getFirst().moduleName()).isEqualTo("govos-api");
        assertThat(modules.getFirst().status()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldMapHealthFromActuatorComponents() {
        when(compositeHealth.getComponents()).thenReturn(Map.of(
                "db", Health.up().build(),
                "diskSpace", Health.up().build()));

        var response = platformMapper.toHealthResponse(compositeHealth);

        assertThat(response.database()).isEqualTo("UP");
        assertThat(response.disk()).isEqualTo("UP");
        assertThat(response.redis()).isEqualTo("NOT_CONFIGURED");
        assertThat(response.uptime()).isNotBlank();
    }
}

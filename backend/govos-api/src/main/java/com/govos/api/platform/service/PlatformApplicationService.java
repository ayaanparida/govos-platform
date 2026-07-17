package com.govos.api.platform.service;

import com.govos.api.platform.config.PlatformProperties;
import com.govos.api.platform.mapper.PlatformMapper;
import com.govos.api.platform.response.BuildResponse;
import com.govos.api.platform.response.HealthResponse;
import com.govos.api.platform.response.ModuleResponse;
import com.govos.api.platform.response.PlatformInfoResponse;
import com.govos.api.platform.response.PlatformVersionResponse;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.List;
import java.util.Properties;

@Service
public class PlatformApplicationService {

    private static final String UNKNOWN = "unknown";

    private final Environment environment;
    private final PlatformProperties platformProperties;
    private final PlatformMapper platformMapper;
    private final HealthEndpoint healthEndpoint;
    private final ObjectProvider<BuildProperties> buildPropertiesProvider;
    private final ObjectProvider<GitProperties> gitPropertiesProvider;
    private final ObjectProvider<Flyway> flywayProvider;
    private final ObjectProvider<DataSource> dataSourceProvider;

    public PlatformApplicationService(
            Environment environment,
            PlatformProperties platformProperties,
            PlatformMapper platformMapper,
            HealthEndpoint healthEndpoint,
            ObjectProvider<BuildProperties> buildPropertiesProvider,
            ObjectProvider<GitProperties> gitPropertiesProvider,
            ObjectProvider<Flyway> flywayProvider,
            ObjectProvider<DataSource> dataSourceProvider) {
        this.environment = environment;
        this.platformProperties = platformProperties;
        this.platformMapper = platformMapper;
        this.healthEndpoint = healthEndpoint;
        this.buildPropertiesProvider = buildPropertiesProvider;
        this.gitPropertiesProvider = gitPropertiesProvider;
        this.flywayProvider = flywayProvider;
        this.dataSourceProvider = dataSourceProvider;
    }

    public PlatformInfoResponse getPlatformInfo() {
        BuildProperties buildProperties = requireBuildProperties();
        GitProperties gitProperties = gitPropertiesProvider.getIfAvailable();
        return platformMapper.toPlatformInfo(
                environment,
                buildProperties,
                gitProperties,
                resolveDatabaseProduct(),
                resolveFlywayVersion());
    }

    public PlatformVersionResponse getPlatformVersion() {
        return platformMapper.toPlatformVersion(
                environment,
                requireBuildProperties(),
                platformProperties);
    }

    public List<ModuleResponse> getModules() {
        return platformMapper.toModuleResponses(platformProperties);
    }

    public BuildResponse getBuild() {
        GitProperties gitProperties = gitPropertiesProvider.getIfAvailable();
        return platformMapper.toBuildResponse(requireBuildProperties(), gitProperties);
    }

    public HealthResponse getHealth() {
        HealthComponent healthComponent = healthEndpoint.health();
        return platformMapper.toHealthResponse(healthComponent);
    }

    private BuildProperties requireBuildProperties() {
        BuildProperties buildProperties = buildPropertiesProvider.getIfAvailable();
        if (buildProperties != null) {
            return buildProperties;
        }

        Properties properties = new Properties();
        String applicationName = environment.getProperty("spring.application.name", "govos-api");
        properties.setProperty("group", "com.govos");
        properties.setProperty("artifact", applicationName);
        properties.setProperty("name", applicationName);
        properties.setProperty("version", environment.getProperty("info.app.version", UNKNOWN));
        return new BuildProperties(properties);
    }

    private String resolveFlywayVersion() {
        Flyway flyway = flywayProvider.getIfAvailable();
        if (flyway == null) {
            return UNKNOWN;
        }

        MigrationInfoService migrationInfoService = flyway.info();
        MigrationInfo current = migrationInfoService.current();
        if (current != null && current.getVersion() != null) {
            return current.getVersion().getVersion();
        }

        MigrationInfo[] applied = migrationInfoService.applied();
        if (applied.length == 0) {
            return UNKNOWN;
        }
        MigrationInfo latest = applied[applied.length - 1];
        return latest.getVersion() != null ? latest.getVersion().getVersion() : UNKNOWN;
    }

    private String resolveDatabaseProduct() {
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) {
            return UNKNOWN;
        }

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            return metaData.getDatabaseProductName();
        } catch (Exception ex) {
            return UNKNOWN;
        }
    }
}

package com.govos.infrastructure.config;

import com.govos.infrastructure.persistence.PersistenceConstants;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flyway database migration configuration.
 */
@Configuration
public class FlywayConfig {

    @Bean
    public FlywayConfigurationCustomizer govosFlywayConfigurationCustomizer() {
        return this::applyGovosFlywayDefaults;
    }

    private void applyGovosFlywayDefaults(FluentConfiguration configuration) {
        configuration
                .schemas(PersistenceConstants.SCHEMA_NAME)
                .defaultSchema(PersistenceConstants.SCHEMA_NAME)
                .locations(PersistenceConstants.FLYWAY_MIGRATION_LOCATION)
                .table(PersistenceConstants.FLYWAY_HISTORY_TABLE)
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .validateMigrationNaming(true)
                .outOfOrder(false)
                .cleanDisabled(true);
    }

}

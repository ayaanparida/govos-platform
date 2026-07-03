package com.govos.infrastructure.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;

import com.govos.infrastructure.audit.JpaAuditingConfig;
import com.govos.infrastructure.persistence.PersistenceConstants;

/**
 * Auto-configuration entry point for the GovOS infrastructure module.
 * <p>
 * Activated when {@code govos-infrastructure} is on the classpath.
 */
@AutoConfiguration
@EntityScan(basePackages = PersistenceConstants.ENTITY_SCAN_BASE_PACKAGE)
@Import({
        DatasourceConfig.class,
        JpaConfig.class,
        FlywayConfig.class,
        JpaAuditingConfig.class
})
public class GovosInfrastructureAutoConfiguration {
}

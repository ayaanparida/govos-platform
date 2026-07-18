package com.govos.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * PostgreSQL datasource configuration.
 * <p>
 * Datasource and HikariCP pool settings are provided via {@code application.yml}
 * and configured by Spring Boot {@code DataSourceAutoConfiguration}.
 */
@Configuration
@EnableTransactionManagement
public class DatasourceConfig {
}

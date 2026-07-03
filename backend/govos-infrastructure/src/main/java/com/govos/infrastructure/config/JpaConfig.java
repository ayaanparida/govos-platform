package com.govos.infrastructure.config;

import com.govos.infrastructure.jpa.GovosPhysicalNamingStrategy;
import com.govos.infrastructure.persistence.PersistenceConstants;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA and Hibernate configuration for GovOS.
 */
@Configuration
@EnableJpaRepositories(basePackages = PersistenceConstants.REPOSITORY_SCAN_BASE_PACKAGE)
public class JpaConfig {

  private static final String IMPLICIT_NAMING_STRATEGY =
          ImplicitNamingStrategyJpaCompliantImpl.class.getName();

  @Bean
  public HibernatePropertiesCustomizer govosHibernatePropertiesCustomizer() {
    return hibernateProperties -> {
      hibernateProperties.put(AvailableSettings.PHYSICAL_NAMING_STRATEGY, GovosPhysicalNamingStrategy.class.getName());
      hibernateProperties.put(AvailableSettings.IMPLICIT_NAMING_STRATEGY, IMPLICIT_NAMING_STRATEGY);
      hibernateProperties.put(AvailableSettings.DEFAULT_SCHEMA, PersistenceConstants.SCHEMA_NAME);
      hibernateProperties.put(AvailableSettings.HBM2DDL_AUTO, "validate");
      hibernateProperties.put(AvailableSettings.SHOW_SQL, false);
      hibernateProperties.put(AvailableSettings.FORMAT_SQL, true);
      hibernateProperties.put(AvailableSettings.USE_SQL_COMMENTS, false);
      hibernateProperties.put(AvailableSettings.GENERATE_STATISTICS, false);
      hibernateProperties.put(AvailableSettings.STATEMENT_BATCH_SIZE, PersistenceConstants.JDBC_BATCH_SIZE);
      hibernateProperties.put("hibernate.order_inserts", true);
      hibernateProperties.put("hibernate.order_updates", true);
    };
  }

}

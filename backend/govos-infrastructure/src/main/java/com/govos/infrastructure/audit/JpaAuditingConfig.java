package com.govos.infrastructure.audit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * Enables JPA auditing infrastructure.
 * <p>
 * Auditor resolution will be replaced by {@code govos-security} in Sprint 0 Day 2+.
 * Until then, a system placeholder is used so audit columns can be wired without security.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "govosAuditorAware")
public class JpaAuditingConfig {

    private static final String SYSTEM_AUDITOR = "system";

    @Bean
    public AuditorAware<String> govosAuditorAware() {
        return () -> Optional.of(SYSTEM_AUDITOR);
    }

}

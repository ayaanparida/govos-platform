package com.govos.security.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Phase 1 security module wiring — password encoding and configuration properties only.
 * <p>
 * Filter chain, authentication manager, and JWT beans are deferred to later phases.
 */
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityModuleConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder(SecurityProperties securityProperties) {
        int strength = securityProperties.getPassword().getBcryptStrength();
        return new BCryptPasswordEncoder(strength);
    }
}

package com.govos.security.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnWebApplication
@Import({
        SecurityModuleConfiguration.class,
        SecurityFilterChainConfiguration.class
})
public class SecurityAutoConfiguration {
}

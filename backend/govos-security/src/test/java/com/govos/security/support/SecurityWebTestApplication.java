package com.govos.security.support;

import com.govos.security.config.SecurityAutoConfiguration;
import com.govos.security.config.SecurityFilterChainConfiguration;
import com.govos.security.config.SecurityModuleConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        SecurityAutoConfiguration.class
})
@Import({
        SecurityModuleConfiguration.class,
        SecurityFilterChainConfiguration.class
})
@ComponentScan(basePackages = {
        "com.govos.security.filter",
        "com.govos.security.handler",
        "com.govos.security.matcher",
        "com.govos.security.resolver",
        "com.govos.security.jwt"
})
public class SecurityWebTestApplication {
}

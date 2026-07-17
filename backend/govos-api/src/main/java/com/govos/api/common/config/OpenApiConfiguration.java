package com.govos.api.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration for the GovOS platform API.
 * <p>
 * JWT security scheme is declared as a placeholder for Phase 3 — no enforcement in this sprint.
 */
@Configuration
public class OpenApiConfiguration {

    public static final String BEARER_AUTH_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI govosOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("GovOS API")
                        .version("0.1.0")
                        .description("Enterprise Government Operating System — REST API")
                        .contact(new Contact().name("GovOS Architecture Team"))
                        .license(new License().name("Proprietary")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT access token (placeholder — enforcement in Security Phase 3)")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME));
    }
}

package com.govos.mdm.config;

import org.springframework.context.annotation.Configuration;

/**
 * MDM bounded-context configuration.
 * <p>
 * Entity and repository scanning is provided by {@code govos-infrastructure}
 * ({@code com.govos} base package). Service-layer beans are discovered via
 * component scanning from {@code govos-api}.
 */
@Configuration
public class MdmConfig {
}

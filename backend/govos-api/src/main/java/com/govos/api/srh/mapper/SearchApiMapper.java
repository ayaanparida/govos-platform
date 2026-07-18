package com.govos.api.srh.mapper;

import org.springframework.stereotype.Component;

/**
 * Maps SRH API request records to domain DTOs and injects {@code JwtPrincipal} values where required.
 * CRUD endpoints reuse {@code com.govos.srh.dto} request records directly when no actor fields are needed.
 */
@Component
public class SearchApiMapper {
}

package com.govos.org.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignUserOrganizationRequest(
        @NotNull
        UUID userId,
        @NotNull
        UUID organizationId,
        Boolean defaultOrganization,
        Boolean active
) {
}

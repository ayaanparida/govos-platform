package com.govos.wrk.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateWorkflowVersionRequest(
        @Size(max = 100)
        String code,
        @NotNull
        UUID definitionId,
        @NotNull
        Integer versionNumber,
        Boolean published,
        Boolean active
) {
}

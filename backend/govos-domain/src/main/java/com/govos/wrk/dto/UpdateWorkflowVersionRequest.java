package com.govos.wrk.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateWorkflowVersionRequest(
        @Size(max = 100)
        String code,
        @NotNull
        Integer versionNumber,
        Boolean published,
        Boolean active,
        Long version
) {
}

package com.govos.org.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateDepartmentHierarchyRequest(
        @NotNull
        UUID parentDepartmentId,
        @NotNull
        UUID childDepartmentId,
        Boolean active
) {
}

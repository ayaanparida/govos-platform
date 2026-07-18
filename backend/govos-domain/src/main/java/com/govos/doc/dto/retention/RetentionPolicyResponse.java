package com.govos.doc.dto.retention;

import com.govos.doc.enums.RetentionAction;

import java.util.UUID;

public record RetentionPolicyResponse(
        UUID id,
        String code,
        String name,
        UUID organizationId,
        Integer retentionDays,
        RetentionAction actionOnExpiry,
        Boolean legalHold,
        String description,
        Boolean active,
        Long version
) {
}

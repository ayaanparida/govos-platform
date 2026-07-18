package com.govos.doc.dto.retention;

import com.govos.doc.enums.RetentionAction;

public record UpdateRetentionPolicyRequest(
        String name,
        Integer retentionDays,
        RetentionAction actionOnExpiry,
        Boolean legalHold,
        String description,
        Boolean active,
        Long version
) {
}

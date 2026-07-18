package com.govos.doc.dto.document;

import java.util.UUID;

public record DocumentReferenceDto(
        UUID id,
        String code,
        String title,
        String documentNumber,
        UUID organizationId
) {
}

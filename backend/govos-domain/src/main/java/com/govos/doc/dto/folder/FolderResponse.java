package com.govos.doc.dto.folder;

import java.util.UUID;

public record FolderResponse(
        UUID id,
        String code,
        String name,
        UUID organizationId,
        UUID ownerId,
        UUID parentFolderId,
        String materializedPath,
        Integer depthLevel,
        Boolean active,
        Long version
) {
}

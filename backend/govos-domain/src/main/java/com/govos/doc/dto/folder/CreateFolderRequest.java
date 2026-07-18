package com.govos.doc.dto.folder;

import java.util.UUID;

public record CreateFolderRequest(
        String name,
        UUID organizationId,
        UUID ownerId,
        UUID parentFolderId,
        String materializedPath,
        Integer depthLevel,
        String code,
        Boolean active
) {
}

package com.govos.doc.dto.folder;

import java.util.List;
import java.util.UUID;

public record FolderTreeResponse(
        UUID id,
        String code,
        String name,
        UUID organizationId,
        UUID parentFolderId,
        String materializedPath,
        Integer depthLevel,
        List<FolderTreeResponse> children
) {
}

package com.govos.doc.dto.folder;

import java.util.UUID;

public record UpdateFolderRequest(
        String name,
        UUID parentFolderId,
        String materializedPath,
        Integer depthLevel,
        Boolean active,
        Long version
) {
}

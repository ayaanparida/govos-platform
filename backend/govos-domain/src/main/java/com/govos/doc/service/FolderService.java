package com.govos.doc.service;

import com.govos.doc.dto.folder.CreateFolderRequest;
import com.govos.doc.dto.folder.UpdateFolderRequest;
import com.govos.doc.entity.Folder;

import java.util.UUID;

public interface FolderService {

    Folder createFolder(CreateFolderRequest request);

    Folder renameFolder(UUID id, String name, Long version);

    Folder moveFolder(UUID id, UUID parentFolderId, Long version);

    void deleteFolder(UUID id);

    Folder restoreFolder(UUID id);

    Folder findFolder(UUID id);
}

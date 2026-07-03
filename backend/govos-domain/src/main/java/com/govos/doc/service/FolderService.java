package com.govos.doc.service;

import com.govos.doc.dto.CreateFolderRequest;
import com.govos.doc.dto.FolderDto;
import com.govos.doc.dto.UpdateFolderRequest;

import java.util.List;
import java.util.UUID;

public interface FolderService {

    FolderDto getById(UUID id);

    FolderDto getByCode(String code);

    List<FolderDto> getAll();

    List<FolderDto> getByOwnerId(UUID ownerId);

    List<FolderDto> getByParentFolderId(UUID parentFolderId);

    FolderDto create(CreateFolderRequest request);

    FolderDto update(UUID id, UpdateFolderRequest request);

    void softDelete(UUID id);
}

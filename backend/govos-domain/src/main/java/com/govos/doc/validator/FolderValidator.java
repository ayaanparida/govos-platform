package com.govos.doc.validator;

import com.govos.doc.dto.CreateFolderRequest;
import com.govos.doc.dto.UpdateFolderRequest;
import com.govos.doc.exception.DuplicateCodeException;
import com.govos.doc.repository.FolderRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FolderValidator {

    private final FolderRepository folderRepository;

    public FolderValidator(FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }

    public void validateCreate(CreateFolderRequest request) {
        if (folderRepository.existsByCodeAndDeletedFalse(request.code())) {
            throw new DuplicateCodeException("Folder", request.code());
        }
    }

    public void validateUpdate(UUID id, UpdateFolderRequest request) {
        folderRepository.findByCodeAndDeletedFalse(request.code())
                .filter(folder -> !folder.getId().equals(id))
                .ifPresent(folder -> {
                    throw new DuplicateCodeException("Folder", request.code());
                });
    }
}

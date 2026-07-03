package com.govos.doc.validator;

import com.govos.doc.dto.CreateDocumentVersionRequest;
import com.govos.doc.dto.UpdateDocumentVersionRequest;
import com.govos.doc.exception.DuplicateAssignmentException;
import com.govos.doc.repository.DocumentVersionRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DocumentVersionValidator {

    private final DocumentVersionRepository documentVersionRepository;

    public DocumentVersionValidator(DocumentVersionRepository documentVersionRepository) {
        this.documentVersionRepository = documentVersionRepository;
    }

    public void validateCreate(CreateDocumentVersionRequest request) {
        if (documentVersionRepository.existsByDocument_IdAndVersionNumberAndDeletedFalse(
                request.documentId(), request.versionNumber())) {
            throw new DuplicateAssignmentException(
                    "Document version already exists: document=" + request.documentId()
                            + ", version=" + request.versionNumber());
        }
    }

    public void validateUpdate(UUID id, UUID documentId, UpdateDocumentVersionRequest request) {
        documentVersionRepository.findByDocument_IdAndVersionNumberAndDeletedFalse(
                        documentId, request.versionNumber())
                .filter(version -> !version.getId().equals(id))
                .ifPresent(version -> {
                    throw new DuplicateAssignmentException(
                            "Document version already exists: document=" + documentId
                                    + ", version=" + request.versionNumber());
                });
    }
}

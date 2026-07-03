package com.govos.doc.validator;

import com.govos.doc.dto.CreateDocumentTagMappingRequest;
import com.govos.doc.exception.DuplicateAssignmentException;
import com.govos.doc.repository.DocumentTagMappingRepository;
import org.springframework.stereotype.Component;

@Component
public class DocumentTagMappingValidator {

    private final DocumentTagMappingRepository documentTagMappingRepository;

    public DocumentTagMappingValidator(DocumentTagMappingRepository documentTagMappingRepository) {
        this.documentTagMappingRepository = documentTagMappingRepository;
    }

    public void validateCreate(CreateDocumentTagMappingRequest request) {
        if (documentTagMappingRepository.existsByDocument_IdAndTag_IdAndDeletedFalse(
                request.documentId(), request.tagId())) {
            throw new DuplicateAssignmentException(
                    "Tag already assigned to document: document=" + request.documentId()
                            + ", tag=" + request.tagId());
        }
    }
}

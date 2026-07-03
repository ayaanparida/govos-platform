package com.govos.doc.validator;

import com.govos.doc.dto.CreateDocumentRequest;
import com.govos.doc.dto.UpdateDocumentRequest;
import com.govos.doc.exception.DuplicateCodeException;
import com.govos.doc.repository.DocumentRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DocumentValidator {

    private final DocumentRepository documentRepository;

    public DocumentValidator(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public void validateCreate(CreateDocumentRequest request) {
        if (documentRepository.existsByCodeAndDeletedFalse(request.code())) {
            throw new DuplicateCodeException("Document", request.code());
        }
    }

    public void validateUpdate(UUID id, UpdateDocumentRequest request) {
        documentRepository.findByCodeAndDeletedFalse(request.code())
                .filter(doc -> !doc.getId().equals(id))
                .ifPresent(doc -> {
                    throw new DuplicateCodeException("Document", request.code());
                });
    }
}

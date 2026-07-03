package com.govos.doc.validator;

import com.govos.doc.dto.CreateDocumentTagRequest;
import com.govos.doc.dto.UpdateDocumentTagRequest;
import com.govos.doc.exception.DuplicateTagNameException;
import com.govos.doc.repository.DocumentTagRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DocumentTagValidator {

    private final DocumentTagRepository documentTagRepository;

    public DocumentTagValidator(DocumentTagRepository documentTagRepository) {
        this.documentTagRepository = documentTagRepository;
    }

    public void validateCreate(CreateDocumentTagRequest request) {
        if (documentTagRepository.existsByNameAndDeletedFalse(request.name())) {
            throw new DuplicateTagNameException(request.name());
        }
    }

    public void validateUpdate(UUID id, UpdateDocumentTagRequest request) {
        documentTagRepository.findByNameAndDeletedFalse(request.name())
                .filter(tag -> !tag.getId().equals(id))
                .ifPresent(tag -> {
                    throw new DuplicateTagNameException(request.name());
                });
    }
}

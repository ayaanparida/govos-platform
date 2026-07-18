package com.govos.srh.validator;

import com.govos.srh.dto.SearchDocumentCreateRequest;
import com.govos.srh.dto.SearchDocumentUpdateRequest;
import com.govos.srh.exception.SearchDocumentException;
import com.govos.srh.exception.SearchIndexNotFoundException;
import com.govos.srh.repository.SearchIndexRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Component
public class SearchDocumentValidator {

    private static final int METADATA_ENTITY_TYPE_MAX_LENGTH = 100;
    private static final int METADATA_REFERENCE_CODE_MAX_LENGTH = 100;

    private final SearchIndexRepository searchIndexRepository;

    public SearchDocumentValidator(SearchIndexRepository searchIndexRepository) {
        this.searchIndexRepository = searchIndexRepository;
    }

    public void validateCreate(SearchDocumentCreateRequest request) {
        requireSearchIndex(request.searchIndexId());
        validateReferenceIdRequired(request.referenceId());
        validateEntityTypeRequired(request.entityType());
        validateOrganizationIdRequired(request.organizationId());
        validateMetadata(
                request.metadataOrganizationId(),
                request.metadataEntityType(),
                request.metadataReferenceId(),
                request.metadataReferenceCode(),
                request.metadataMappingVersion(),
                request.metadataIndexedAt(),
                request.metadataLastIndexedAt());
    }

    public void validateUpdate(SearchDocumentUpdateRequest request) {
        validateReferenceIdRequired(request.referenceId());
        validateEntityTypeRequired(request.entityType());
        validateOrganizationIdRequired(request.organizationId());
        validateMetadata(
                request.metadataOrganizationId(),
                request.metadataEntityType(),
                request.metadataReferenceId(),
                request.metadataReferenceCode(),
                request.metadataMappingVersion(),
                request.metadataIndexedAt(),
                request.metadataLastIndexedAt());
    }

    private void requireSearchIndex(UUID searchIndexId) {
        if (searchIndexId == null) {
            throw new SearchDocumentException("Search index is required for search document");
        }
        searchIndexRepository.findByIdAndDeletedFalse(searchIndexId)
                .orElseThrow(() -> new SearchIndexNotFoundException(
                        "Search index not found with id: " + searchIndexId));
    }

    private void validateReferenceIdRequired(UUID referenceId) {
        if (referenceId == null) {
            throw new SearchDocumentException("Search document reference id is required");
        }
    }

    private void validateEntityTypeRequired(String entityType) {
        if (!StringUtils.hasText(entityType)) {
            throw new SearchDocumentException("Search document entity type is required");
        }
    }

    private void validateOrganizationIdRequired(UUID organizationId) {
        if (organizationId == null) {
            throw new SearchDocumentException("Search document organization id is required");
        }
    }

    private void validateMetadata(
            UUID metadataOrganizationId,
            String metadataEntityType,
            UUID metadataReferenceId,
            String metadataReferenceCode,
            Integer metadataMappingVersion,
            java.time.Instant metadataIndexedAt,
            java.time.Instant metadataLastIndexedAt) {
        if (metadataOrganizationId == null
                && !StringUtils.hasText(metadataEntityType)
                && metadataReferenceId == null
                && !StringUtils.hasText(metadataReferenceCode)
                && metadataMappingVersion == null
                && metadataIndexedAt == null
                && metadataLastIndexedAt == null) {
            return;
        }
        if (StringUtils.hasText(metadataEntityType)
                && metadataEntityType.length() > METADATA_ENTITY_TYPE_MAX_LENGTH) {
            throw new SearchDocumentException(
                    "Search document metadata entity type exceeds maximum length of "
                            + METADATA_ENTITY_TYPE_MAX_LENGTH);
        }
        if (StringUtils.hasText(metadataReferenceCode)
                && metadataReferenceCode.length() > METADATA_REFERENCE_CODE_MAX_LENGTH) {
            throw new SearchDocumentException(
                    "Search document metadata reference code exceeds maximum length of "
                            + METADATA_REFERENCE_CODE_MAX_LENGTH);
        }
        if (metadataMappingVersion != null && metadataMappingVersion <= 0) {
            throw new SearchDocumentException(
                    "Search document metadata mapping version must be greater than zero");
        }
    }
}

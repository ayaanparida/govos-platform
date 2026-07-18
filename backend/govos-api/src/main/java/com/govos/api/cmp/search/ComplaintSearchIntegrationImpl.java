package com.govos.api.cmp.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.api.srh.application.SearchApplicationService;
import com.govos.cmp.dto.ComplaintDto;
import com.govos.srh.dto.SearchDocumentCreateRequest;
import com.govos.srh.dto.SearchDocumentDto;
import com.govos.srh.dto.SearchDocumentUpdateRequest;
import com.govos.srh.dto.SearchIndexDto;
import com.govos.srh.exception.SearchException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class ComplaintSearchIntegrationImpl implements ComplaintSearchIntegration {

    public static final String INDEX_CODE = "CMP_COMPLAINT";
    public static final String ENTITY_TYPE = "COMPLAINT";

    private final SearchApplicationService searchApplicationService;
    private final ObjectMapper objectMapper;
    private final ComplaintSearchMapper complaintSearchMapper;

    private volatile SearchIndexDto cachedSearchIndex;

    public ComplaintSearchIntegrationImpl(
            SearchApplicationService searchApplicationService,
            ObjectMapper objectMapper,
            ComplaintSearchMapper complaintSearchMapper) {
        this.searchApplicationService = searchApplicationService;
        this.objectMapper = objectMapper;
        this.complaintSearchMapper = complaintSearchMapper;
    }

    @Override
    public void onCreated(ComplaintDto complaint) {
        createSearchDocument(ComplaintSearchDocument.from(complaint));
    }

    @Override
    public void onUpdated(ComplaintDto complaint) {
        upsertSearchDocument(complaint, ComplaintSearchDocument.from(complaint));
    }

    @Override
    public void onSubmitted(ComplaintDto complaint) {
        upsertSearchDocument(complaint, ComplaintSearchDocument.from(complaint));
    }

    @Override
    public void onAssigned(ComplaintDto complaint) {
        upsertSearchDocument(complaint, ComplaintSearchDocument.from(complaint));
    }

    @Override
    public void onInProgress(ComplaintDto complaint) {
        upsertSearchDocument(complaint, ComplaintSearchDocument.from(complaint));
    }

    @Override
    public void onResolved(ComplaintDto complaint) {
        upsertSearchDocument(complaint, ComplaintSearchDocument.from(complaint));
    }

    @Override
    public void onClosed(ComplaintDto complaint) {
        upsertSearchDocument(complaint, ComplaintSearchDocument.from(complaint));
    }

    @Override
    public void onArchived(ComplaintDto complaint) {
        upsertSearchDocument(complaint, ComplaintSearchDocument.archived(complaint));
    }

    @Override
    public void onReopened(ComplaintDto complaint) {
        upsertSearchDocument(complaint, ComplaintSearchDocument.from(complaint));
    }

    @Override
    public void onSoftDeleted(ComplaintDto complaint) {
        SearchDocumentDto existing = requireExistingDocument(complaint);
        runVoid(() -> searchApplicationService.softDeleteDocument(existing.id()));
    }

    @Override
    public void onRestored(ComplaintDto complaint) {
        Optional<SearchDocumentDto> existing = findExistingDocument(complaint);
        if (existing.isPresent()) {
            upsertSearchDocument(complaint, ComplaintSearchDocument.from(complaint), existing.get());
            return;
        }

        UUID restorableId = resolveRestorableDocumentId(complaint);
        SearchDocumentDto restored = runReturning(() -> searchApplicationService.restoreDocument(restorableId));
        upsertSearchDocument(complaint, ComplaintSearchDocument.from(complaint), restored);
    }

    @Override
    public void onCommentAdded(ComplaintDto complaint) {
        upsertSearchDocument(complaint, ComplaintSearchDocument.from(complaint));
    }

    @Override
    public void onAttachmentAdded(ComplaintDto complaint) {
        upsertSearchDocument(complaint, ComplaintSearchDocument.from(complaint));
    }

    @Override
    public void onDuplicateCreated(ComplaintDto complaint) {
        upsertSearchDocument(complaint, ComplaintSearchDocument.from(complaint));
    }

    @Override
    public void onMergeCreated(ComplaintDto complaint) {
        upsertSearchDocument(complaint, ComplaintSearchDocument.from(complaint));
    }

    private void createSearchDocument(ComplaintSearchDocument document) {
        SearchIndexDto index = resolveSearchIndex();
        SearchDocumentCreateRequest request = complaintSearchMapper.toCreateRequest(
                document,
                index.id(),
                document.toJson(objectMapper),
                index.mappingVersion());
        runVoid(() -> searchApplicationService.createDocument(request));
    }

    private void upsertSearchDocument(ComplaintDto complaint, ComplaintSearchDocument document) {
        findExistingDocument(complaint)
                .ifPresentOrElse(
                        existing -> upsertSearchDocument(complaint, document, existing),
                        () -> createSearchDocument(document));
    }

    private void upsertSearchDocument(
            ComplaintDto complaint,
            ComplaintSearchDocument document,
            SearchDocumentDto existing) {
        SearchIndexDto index = resolveSearchIndex();
        SearchDocumentUpdateRequest request = complaintSearchMapper.toUpdateRequest(
                document,
                document.toJson(objectMapper),
                index.mappingVersion(),
                existing.version());
        runVoid(() -> searchApplicationService.updateDocument(existing.id(), request));
    }

    private SearchDocumentDto requireExistingDocument(ComplaintDto complaint) {
        return findExistingDocument(complaint)
                .orElseThrow(() -> new ComplaintSearchIntegrationException(
                        "Search document not found for complaint: " + complaint.id()));
    }

    private Optional<SearchDocumentDto> findExistingDocument(ComplaintDto complaint) {
        UUID searchIndexId = resolveSearchIndexId();
        return searchApplicationService.listDocumentsByOrganization(complaint.organizationId()).stream()
                .filter(document -> searchIndexId.equals(document.searchIndexId()))
                .filter(document -> ENTITY_TYPE.equals(document.entityType()))
                .filter(document -> complaint.id().equals(document.referenceId()))
                .findFirst();
    }

    private UUID resolveRestorableDocumentId(ComplaintDto complaint) {
        UUID searchIndexId = resolveSearchIndexId();
        Optional<UUID> byReference = searchApplicationService.listDocumentsByOrganization(complaint.organizationId())
                .stream()
                .filter(document -> searchIndexId.equals(document.searchIndexId()))
                .filter(document -> ENTITY_TYPE.equals(document.entityType()))
                .filter(document -> complaint.id().equals(document.referenceId()))
                .map(SearchDocumentDto::id)
                .findFirst();
        if (byReference.isPresent()) {
            return byReference.get();
        }

        return searchApplicationService.listDocumentsByOrganization(complaint.organizationId()).stream()
                .filter(document -> complaint.id().equals(document.searchDocumentId()))
                .map(SearchDocumentDto::id)
                .findFirst()
                .orElse(complaint.id());
    }

    private SearchIndexDto resolveSearchIndex() {
        if (cachedSearchIndex == null) {
            cachedSearchIndex = searchApplicationService.getIndexByCode(INDEX_CODE);
        }
        return cachedSearchIndex;
    }

    private UUID resolveSearchIndexId() {
        return resolveSearchIndex().id();
    }

    private void runVoid(Runnable runnable) {
        try {
            runnable.run();
        } catch (ComplaintSearchIntegrationException ex) {
            throw ex;
        } catch (SearchException ex) {
            throw new ComplaintSearchIntegrationException(
                    "Search integration failed: " + ex.getMessage(), ex);
        }
    }

    private <T> T runReturning(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (ComplaintSearchIntegrationException ex) {
            throw ex;
        } catch (SearchException ex) {
            throw new ComplaintSearchIntegrationException(
                    "Search integration failed: " + ex.getMessage(), ex);
        }
    }
}

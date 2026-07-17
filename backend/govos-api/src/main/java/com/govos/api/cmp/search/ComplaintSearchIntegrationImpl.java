package com.govos.api.cmp.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.cmp.dto.ComplaintDto;
import com.govos.srh.dto.IndexSearchDocumentRequest;
import com.govos.srh.exception.SearchException;
import com.govos.srh.service.SearchIndexService;
import org.springframework.stereotype.Service;

@Service
public class ComplaintSearchIntegrationImpl implements ComplaintSearchIntegration {

    public static final String INDEX_CODE = "CMP_COMPLAINT";
    public static final String ENTITY_TYPE = "COMPLAINT";

    private final SearchIndexService searchIndexService;
    private final ObjectMapper objectMapper;

    public ComplaintSearchIntegrationImpl(
            SearchIndexService searchIndexService,
            ObjectMapper objectMapper) {
        this.searchIndexService = searchIndexService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onCreated(ComplaintDto complaint) {
        index(ComplaintSearchDocument.from(complaint));
    }

    @Override
    public void onUpdated(ComplaintDto complaint) {
        reindex(ComplaintSearchDocument.from(complaint));
    }

    @Override
    public void onSubmitted(ComplaintDto complaint) {
        reindex(ComplaintSearchDocument.from(complaint));
    }

    @Override
    public void onAssigned(ComplaintDto complaint) {
        reindex(ComplaintSearchDocument.from(complaint));
    }

    @Override
    public void onInProgress(ComplaintDto complaint) {
        reindex(ComplaintSearchDocument.from(complaint));
    }

    @Override
    public void onResolved(ComplaintDto complaint) {
        reindex(ComplaintSearchDocument.from(complaint));
    }

    @Override
    public void onClosed(ComplaintDto complaint) {
        reindex(ComplaintSearchDocument.from(complaint));
    }

    @Override
    public void onArchived(ComplaintDto complaint) {
        reindex(ComplaintSearchDocument.archived(complaint));
    }

    @Override
    public void onReopened(ComplaintDto complaint) {
        reindex(ComplaintSearchDocument.from(complaint));
    }

    @Override
    public void onSoftDeleted(ComplaintDto complaint) {
        runVoid(() -> searchIndexService.remove(INDEX_CODE, complaint.id()));
    }

    @Override
    public void onRestored(ComplaintDto complaint) {
        index(ComplaintSearchDocument.from(complaint));
    }

    @Override
    public void onCommentAdded(ComplaintDto complaint) {
        reindex(ComplaintSearchDocument.from(complaint));
    }

    @Override
    public void onAttachmentAdded(ComplaintDto complaint) {
        reindex(ComplaintSearchDocument.from(complaint));
    }

    @Override
    public void onDuplicateCreated(ComplaintDto complaint) {
        reindex(ComplaintSearchDocument.from(complaint));
    }

    @Override
    public void onMergeCreated(ComplaintDto complaint) {
        reindex(ComplaintSearchDocument.from(complaint));
    }

    private void index(ComplaintSearchDocument document) {
        runVoid(() -> searchIndexService.index(toRequest(document)));
    }

    private void reindex(ComplaintSearchDocument document) {
        runVoid(() -> searchIndexService.reindex(toRequest(document)));
    }

    private IndexSearchDocumentRequest toRequest(ComplaintSearchDocument document) {
        return new IndexSearchDocumentRequest(
                INDEX_CODE,
                document.complaintId(),
                ENTITY_TYPE,
                document.toJson(objectMapper));
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
}

package com.govos.api.cmp.search;

import com.govos.srh.dto.SearchDocumentCreateRequest;
import com.govos.srh.dto.SearchDocumentUpdateRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class ComplaintSearchMapper {

    public SearchDocumentCreateRequest toCreateRequest(
            ComplaintSearchDocument document,
            UUID searchIndexId,
            String documentJson,
            Integer mappingVersion) {
        Instant now = Instant.now();
        return new SearchDocumentCreateRequest(
                document.complaintCode(),
                searchIndexId,
                document.complaintId(),
                ComplaintSearchIntegrationImpl.ENTITY_TYPE,
                document.complaintId(),
                document.complaintCode(),
                document.organizationId(),
                documentJson,
                document.searchText(),
                document.searchVersion(),
                now,
                now,
                document.organizationId(),
                ComplaintSearchIntegrationImpl.ENTITY_TYPE,
                document.complaintId(),
                document.complaintCode(),
                mappingVersion,
                now,
                now,
                document.active());
    }

    public SearchDocumentUpdateRequest toUpdateRequest(
            ComplaintSearchDocument document,
            String documentJson,
            Integer mappingVersion,
            Long version) {
        Instant now = Instant.now();
        return new SearchDocumentUpdateRequest(
                document.complaintCode(),
                ComplaintSearchIntegrationImpl.ENTITY_TYPE,
                document.complaintId(),
                document.complaintCode(),
                document.organizationId(),
                documentJson,
                document.searchText(),
                document.searchVersion(),
                now,
                now,
                document.organizationId(),
                ComplaintSearchIntegrationImpl.ENTITY_TYPE,
                document.complaintId(),
                document.complaintCode(),
                mappingVersion,
                now,
                now,
                document.active(),
                version);
    }
}

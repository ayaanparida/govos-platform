package com.govos.doc.mapper;

import com.govos.doc.dto.document.CreateDocumentRequest;
import com.govos.doc.dto.document.UpdateDocumentRequest;
import com.govos.doc.entity.Document;
import com.govos.doc.entity.DocumentCategory;
import com.govos.doc.entity.DocumentRetentionPolicy;
import com.govos.doc.entity.DocumentVersion;
import com.govos.doc.entity.Folder;
import com.govos.doc.enums.DocumentClassification;
import com.govos.doc.enums.DocumentStatus;
import com.govos.doc.support.DocumentTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentMapperTest {

    private DocumentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DocumentMapperImpl();
    }

    @Test
    void shouldMapEntityToResponseFlatteningRelations() {
        Document entity = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        entity.setFolder(DocumentTestFixtures.folder(DocumentTestFixtures.FOLDER_ID));
        entity.setCategory(DocumentTestFixtures.category(DocumentTestFixtures.CATEGORY_ID));
        entity.setRetentionPolicy(DocumentTestFixtures.retentionPolicy(DocumentTestFixtures.POLICY_ID));
        entity.setActiveVersion(DocumentTestFixtures.documentVersion(DocumentTestFixtures.VERSION_ID, entity));

        var response = mapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(DocumentTestFixtures.DOCUMENT_ID);
        assertThat(response.folderId()).isEqualTo(DocumentTestFixtures.FOLDER_ID);
        assertThat(response.categoryId()).isEqualTo(DocumentTestFixtures.CATEGORY_ID);
        assertThat(response.retentionPolicyId()).isEqualTo(DocumentTestFixtures.POLICY_ID);
        assertThat(response.activeVersionId()).isEqualTo(DocumentTestFixtures.VERSION_ID);
        assertThat(response.title()).isEqualTo("Test Document");
    }

    @Test
    void shouldMapCreateRequestToEntityIgnoringLifecycleFields() {
        CreateDocumentRequest request = DocumentTestFixtures.createDocumentRequest();

        Document entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getStatus()).isEqualTo(DocumentStatus.UPLOADED);
        assertThat(entity.getFolder()).isNull();
        assertThat(entity.getTitle()).isEqualTo(request.title());
        assertThat(entity.getOrganizationId()).isEqualTo(request.organizationId());
    }

    @Test
    void shouldUpdateEntityIgnoringOrganizationAndRelations() {
        Document entity = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        entity.setOrganizationId(DocumentTestFixtures.ORG_ID);
        entity.setOwnerId(DocumentTestFixtures.OWNER_ID);
        entity.setFolder(new Folder());
        entity.setStatus(DocumentStatus.UPLOADED);

        UpdateDocumentRequest request = DocumentTestFixtures.updateDocumentRequest();
        mapper.updateEntity(request, entity);

        assertThat(entity.getOrganizationId()).isEqualTo(DocumentTestFixtures.ORG_ID);
        assertThat(entity.getOwnerId()).isEqualTo(DocumentTestFixtures.OWNER_ID);
        assertThat(entity.getStatus()).isEqualTo(DocumentStatus.UPLOADED);
        assertThat(entity.getTitle()).isEqualTo("Updated Title");
        assertThat(entity.getClassification()).isEqualTo(DocumentClassification.CONFIDENTIAL);
    }

    @Test
    void shouldHandleNullRelationsInResponse() {
        Document entity = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);

        var response = mapper.toResponse(entity);

        assertThat(response.folderId()).isNull();
        assertThat(response.categoryId()).isNull();
        assertThat(response.retentionPolicyId()).isNull();
        assertThat(response.activeVersionId()).isNull();
    }

    @Test
    void shouldMapSummarySearchReferenceAndListResponses() {
        Document entity = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        entity.setFolder(DocumentTestFixtures.folder(DocumentTestFixtures.FOLDER_ID));

        assertThat(mapper.toSummaryResponse(entity).title()).isEqualTo("Test Document");
        assertThat(mapper.toSearchResponse(entity).folderId()).isEqualTo(DocumentTestFixtures.FOLDER_ID);
        assertThat(mapper.toReferenceDto(entity).id()).isEqualTo(DocumentTestFixtures.DOCUMENT_ID);
        assertThat(mapper.toSummaryResponseList(List.of(entity))).hasSize(1);
        assertThat(mapper.toListResponse(new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1)).totalElements())
                .isEqualTo(1);
    }
}

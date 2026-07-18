package com.govos.doc.entity;

import com.govos.doc.enums.DocumentClassification;
import com.govos.doc.enums.DocumentStatus;
import com.govos.doc.support.DocumentTestFixtures;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentTest {

    @Test
    void shouldExposeFixtureDefaultsAndSoftDeleteFields() {
        Document entity = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);

        assertThat(entity.getId()).isEqualTo(DocumentTestFixtures.DOCUMENT_ID);
        assertThat(entity.getTitle()).isEqualTo("Test Document");
        assertThat(entity.getOrganizationId()).isEqualTo(DocumentTestFixtures.ORG_ID);
        assertThat(entity.getOwnerId()).isEqualTo(DocumentTestFixtures.OWNER_ID);
        assertThat(entity.getStatus()).isEqualTo(DocumentStatus.UPLOADED);
        assertThat(entity.getClassification()).isEqualTo(DocumentClassification.INTERNAL);
        assertThat(entity.getActive()).isTrue();
        assertThat(entity.getDeleted()).isFalse();
        assertThat(entity.getVersion()).isZero();
    }

    @Test
    void shouldWireRelationshipsAndCollections() {
        Document entity = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        Folder folder = DocumentTestFixtures.folder(DocumentTestFixtures.FOLDER_ID);
        DocumentCategory category = DocumentTestFixtures.category(DocumentTestFixtures.CATEGORY_ID);
        DocumentRetentionPolicy policy = DocumentTestFixtures.retentionPolicy(DocumentTestFixtures.POLICY_ID);
        DocumentVersion version = DocumentTestFixtures.documentVersion(DocumentTestFixtures.VERSION_ID, entity);

        entity.setFolder(folder);
        entity.setCategory(category);
        entity.setRetentionPolicy(policy);
        entity.setActiveVersion(version);
        entity.setVersions(new ArrayList<>());
        entity.getVersions().add(version);
        entity.setMetadataEntries(new ArrayList<>());
        entity.setShares(new ArrayList<>());
        entity.setAccessLogs(new ArrayList<>());

        assertThat(entity.getFolder()).isSameAs(folder);
        assertThat(entity.getCategory()).isSameAs(category);
        assertThat(entity.getRetentionPolicy()).isSameAs(policy);
        assertThat(entity.getActiveVersion()).isSameAs(version);
        assertThat(entity.getVersions()).containsExactly(version);
        assertThat(entity.getMetadataEntries()).isEmpty();
        assertThat(entity.getShares()).isEmpty();
        assertThat(entity.getAccessLogs()).isEmpty();
    }

    @Test
    void shouldUpdateScalarFieldsViaSetters() {
        Document entity = new Document();
        entity.setTitle("New Title");
        entity.setDescription("New Description");
        entity.setStatus(DocumentStatus.READY);
        entity.setClassification(DocumentClassification.CONFIDENTIAL);
        entity.setMimeType("text/plain");
        entity.setModuleCode("CMP");
        entity.setEntityType("Complaint");
        entity.setReferenceId(DocumentTestFixtures.USER_ID);
        entity.setDocumentNumber("DOC-999");
        entity.setTags("a,b");
        entity.setDeleted(true);
        entity.setActive(false);
        entity.setVersion(3L);

        assertThat(entity.getTitle()).isEqualTo("New Title");
        assertThat(entity.getDescription()).isEqualTo("New Description");
        assertThat(entity.getStatus()).isEqualTo(DocumentStatus.READY);
        assertThat(entity.getClassification()).isEqualTo(DocumentClassification.CONFIDENTIAL);
        assertThat(entity.getMimeType()).isEqualTo("text/plain");
        assertThat(entity.getModuleCode()).isEqualTo("CMP");
        assertThat(entity.getEntityType()).isEqualTo("Complaint");
        assertThat(entity.getReferenceId()).isEqualTo(DocumentTestFixtures.USER_ID);
        assertThat(entity.getDocumentNumber()).isEqualTo("DOC-999");
        assertThat(entity.getTags()).isEqualTo("a,b");
        assertThat(entity.getDeleted()).isTrue();
        assertThat(entity.getActive()).isFalse();
        assertThat(entity.getVersion()).isEqualTo(3L);
    }
}

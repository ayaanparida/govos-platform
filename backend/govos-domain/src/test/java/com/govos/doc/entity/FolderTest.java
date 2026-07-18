package com.govos.doc.entity;

import com.govos.doc.support.DocumentTestFixtures;
import com.govos.doc.valueobject.DocumentPath;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FolderTest {

    @Test
    void shouldExposeEmbeddedPathMetadataAndDefaults() {
        Folder entity = DocumentTestFixtures.folder(DocumentTestFixtures.FOLDER_ID);

        assertThat(entity.getName()).isEqualTo("Root");
        assertThat(entity.getOrganizationId()).isEqualTo(DocumentTestFixtures.ORG_ID);
        assertThat(entity.getOwnerId()).isEqualTo(DocumentTestFixtures.OWNER_ID);
        assertThat(entity.getPathMetadata().getMaterializedPath()).isEqualTo("/Root");
        assertThat(entity.getPathMetadata().getDepthLevel()).isZero();
        assertThat(entity.getActive()).isTrue();
        assertThat(entity.getDeleted()).isFalse();
        assertThat(entity.getVersion()).isZero();
    }

    @Test
    void shouldWireParentRelationshipAndUpdateFields() {
        Folder parent = DocumentTestFixtures.folder(DocumentTestFixtures.FOLDER_ID);
        Folder child = new Folder();
        child.setName("Child");
        child.setParentFolder(parent);
        child.setPathMetadata(new DocumentPath("/Root/Child", 1));
        child.setDeleted(true);
        child.setVersion(2L);

        assertThat(child.getParentFolder()).isSameAs(parent);
        assertThat(child.getPathMetadata().getMaterializedPath()).isEqualTo("/Root/Child");
        assertThat(child.getPathMetadata().getDepthLevel()).isEqualTo(1);
        assertThat(child.getDeleted()).isTrue();
        assertThat(child.getVersion()).isEqualTo(2L);
    }
}

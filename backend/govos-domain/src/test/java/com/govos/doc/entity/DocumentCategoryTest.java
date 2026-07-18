package com.govos.doc.entity;

import com.govos.doc.support.DocumentTestFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentCategoryTest {

    @Test
    void shouldExposeFixtureDefaults() {
        DocumentCategory entity = DocumentTestFixtures.category(DocumentTestFixtures.CATEGORY_ID);

        assertThat(entity.getCode()).isEqualTo("CAT-001");
        assertThat(entity.getName()).isEqualTo("General");
        assertThat(entity.getOrganizationId()).isEqualTo(DocumentTestFixtures.ORG_ID);
        assertThat(entity.getActive()).isTrue();
        assertThat(entity.getDeleted()).isFalse();
        assertThat(entity.getVersion()).isZero();
    }

    @Test
    void shouldWireHierarchyAndRetentionPolicy() {
        DocumentCategory parent = DocumentTestFixtures.category(DocumentTestFixtures.CATEGORY_ID);
        DocumentRetentionPolicy policy = DocumentTestFixtures.retentionPolicy(DocumentTestFixtures.POLICY_ID);
        DocumentCategory child = new DocumentCategory();
        child.setName("Subcategory");
        child.setParentCategory(parent);
        child.setDefaultRetentionPolicy(policy);
        child.setAllowedMimeTypes("application/pdf");
        child.setDescription("Desc");

        assertThat(child.getParentCategory()).isSameAs(parent);
        assertThat(child.getDefaultRetentionPolicy()).isSameAs(policy);
        assertThat(child.getAllowedMimeTypes()).isEqualTo("application/pdf");
        assertThat(child.getDescription()).isEqualTo("Desc");
    }
}

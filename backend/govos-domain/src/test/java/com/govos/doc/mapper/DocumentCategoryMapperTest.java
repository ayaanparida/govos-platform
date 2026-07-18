package com.govos.doc.mapper;

import com.govos.doc.dto.category.CreateDocumentCategoryRequest;
import com.govos.doc.dto.category.UpdateDocumentCategoryRequest;
import com.govos.doc.entity.DocumentCategory;
import com.govos.doc.entity.DocumentRetentionPolicy;
import com.govos.doc.support.DocumentTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentCategoryMapperTest {

    private DocumentCategoryMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DocumentCategoryMapperImpl();
    }

    @Test
    void shouldMapEntityToResponseFlatteningRelations() {
        DocumentCategory entity = DocumentTestFixtures.category(DocumentTestFixtures.CATEGORY_ID);
        entity.setParentCategory(DocumentTestFixtures.category(DocumentTestFixtures.FOLDER_ID));
        entity.setDefaultRetentionPolicy(DocumentTestFixtures.retentionPolicy(DocumentTestFixtures.POLICY_ID));

        var response = mapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(DocumentTestFixtures.CATEGORY_ID);
        assertThat(response.parentCategoryId()).isEqualTo(DocumentTestFixtures.FOLDER_ID);
        assertThat(response.defaultRetentionPolicyId()).isEqualTo(DocumentTestFixtures.POLICY_ID);
    }

    @Test
    void shouldMapCreateRequestToEntityIgnoringRelations() {
        CreateDocumentCategoryRequest request = DocumentTestFixtures.createCategoryRequest();

        DocumentCategory entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getParentCategory()).isNull();
        assertThat(entity.getDefaultRetentionPolicy()).isNull();
        assertThat(entity.getName()).isEqualTo("General");
    }

    @Test
    void shouldUpdateEntityIgnoringCodeAndOrganization() {
        DocumentCategory entity = DocumentTestFixtures.category(DocumentTestFixtures.CATEGORY_ID);

        UpdateDocumentCategoryRequest request = new UpdateDocumentCategoryRequest(
                "Updated", null, null, "application/pdf", "Desc", false, 0L);
        mapper.updateEntity(request, entity);

        assertThat(entity.getCode()).isEqualTo("CAT-001");
        assertThat(entity.getOrganizationId()).isEqualTo(DocumentTestFixtures.ORG_ID);
        assertThat(entity.getName()).isEqualTo("Updated");
        assertThat(entity.getAllowedMimeTypes()).isEqualTo("application/pdf");
    }

    @Test
    void shouldMapResponseListAndHandleNullEntity() {
        DocumentCategory entity = DocumentTestFixtures.category(DocumentTestFixtures.CATEGORY_ID);

        assertThat(mapper.toResponseList(java.util.List.of(entity))).hasSize(1);
        assertThat(mapper.toResponse(null)).isNull();
    }
}

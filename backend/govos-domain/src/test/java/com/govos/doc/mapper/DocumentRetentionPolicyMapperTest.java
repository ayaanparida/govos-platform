package com.govos.doc.mapper;

import com.govos.doc.dto.retention.CreateRetentionPolicyRequest;
import com.govos.doc.dto.retention.UpdateRetentionPolicyRequest;
import com.govos.doc.entity.DocumentRetentionPolicy;
import com.govos.doc.enums.RetentionAction;
import com.govos.doc.support.DocumentTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentRetentionPolicyMapperTest {

    private DocumentRetentionPolicyMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DocumentRetentionPolicyMapperImpl();
    }

    @Test
    void shouldMapEntityToResponse() {
        DocumentRetentionPolicy entity = DocumentTestFixtures.retentionPolicy(DocumentTestFixtures.POLICY_ID);

        var response = mapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(DocumentTestFixtures.POLICY_ID);
        assertThat(response.name()).isEqualTo("Default Policy");
        assertThat(response.retentionDays()).isEqualTo(365);
        assertThat(response.actionOnExpiry()).isEqualTo(RetentionAction.ARCHIVE);
    }

    @Test
    void shouldMapCreateRequestToEntityIgnoringAuditFields() {
        CreateRetentionPolicyRequest request = DocumentTestFixtures.createRetentionPolicyRequest();

        DocumentRetentionPolicy entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getName()).isEqualTo("Default Policy");
        assertThat(entity.getOrganizationId()).isEqualTo(DocumentTestFixtures.ORG_ID);
        assertThat(entity.getRetentionDays()).isEqualTo(365);
    }

    @Test
    void shouldUpdateEntityPreservingOrganization() {
        DocumentRetentionPolicy entity = DocumentTestFixtures.retentionPolicy(DocumentTestFixtures.POLICY_ID);

        UpdateRetentionPolicyRequest request = new UpdateRetentionPolicyRequest(
                "Updated", 90, RetentionAction.DELETE, true, "Desc", false, 0L);
        mapper.updateEntity(request, entity);

        assertThat(entity.getOrganizationId()).isEqualTo(DocumentTestFixtures.ORG_ID);
        assertThat(entity.getName()).isEqualTo("Updated");
        assertThat(entity.getRetentionDays()).isEqualTo(90);
        assertThat(entity.getLegalHold()).isTrue();
    }

    @Test
    void shouldMapResponseListAndHandleNullEntity() {
        DocumentRetentionPolicy entity = DocumentTestFixtures.retentionPolicy(DocumentTestFixtures.POLICY_ID);

        assertThat(mapper.toResponseList(java.util.List.of(entity))).hasSize(1);
        assertThat(mapper.toResponse(null)).isNull();
    }
}

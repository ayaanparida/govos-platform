package com.govos.doc.entity;

import com.govos.doc.enums.RetentionAction;
import com.govos.doc.support.DocumentTestFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentRetentionPolicyTest {

    @Test
    void shouldExposeFixtureDefaults() {
        DocumentRetentionPolicy entity = DocumentTestFixtures.retentionPolicy(DocumentTestFixtures.POLICY_ID);

        assertThat(entity.getName()).isEqualTo("Default Policy");
        assertThat(entity.getOrganizationId()).isEqualTo(DocumentTestFixtures.ORG_ID);
        assertThat(entity.getRetentionDays()).isEqualTo(365);
        assertThat(entity.getActionOnExpiry()).isEqualTo(RetentionAction.ARCHIVE);
        assertThat(entity.getLegalHold()).isFalse();
        assertThat(entity.getActive()).isTrue();
        assertThat(entity.getDeleted()).isFalse();
        assertThat(entity.getVersion()).isZero();
    }

    @Test
    void shouldUpdateFieldsViaSetters() {
        DocumentRetentionPolicy entity = new DocumentRetentionPolicy();
        entity.setName("Updated");
        entity.setRetentionDays(90);
        entity.setActionOnExpiry(RetentionAction.DELETE);
        entity.setLegalHold(true);
        entity.setDescription("Updated policy");
        entity.setDeleted(true);
        entity.setVersion(1L);

        assertThat(entity.getName()).isEqualTo("Updated");
        assertThat(entity.getRetentionDays()).isEqualTo(90);
        assertThat(entity.getActionOnExpiry()).isEqualTo(RetentionAction.DELETE);
        assertThat(entity.getLegalHold()).isTrue();
        assertThat(entity.getDescription()).isEqualTo("Updated policy");
        assertThat(entity.getDeleted()).isTrue();
        assertThat(entity.getVersion()).isEqualTo(1L);
    }
}

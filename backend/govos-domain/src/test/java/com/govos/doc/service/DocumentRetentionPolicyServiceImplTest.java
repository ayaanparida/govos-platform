package com.govos.doc.service.impl;

import com.govos.doc.dto.retention.CreateRetentionPolicyRequest;
import com.govos.doc.dto.retention.UpdateRetentionPolicyRequest;
import com.govos.doc.entity.DocumentRetentionPolicy;
import com.govos.doc.enums.RetentionAction;
import com.govos.doc.event.publisher.DocumentEventPublisher;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.exception.RetentionPolicyNotFoundException;
import com.govos.doc.mapper.DocumentRetentionPolicyMapper;
import com.govos.doc.repository.DocumentRetentionPolicyRepository;
import com.govos.doc.support.DocumentTestFixtures;
import com.govos.doc.validator.DocumentRetentionPolicyValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentRetentionPolicyServiceImplTest {

    @Mock private DocumentRetentionPolicyRepository retentionPolicyRepository;
    @Mock private DocumentRetentionPolicyMapper retentionPolicyMapper;
    @Mock private DocumentRetentionPolicyValidator retentionPolicyValidator;
    @Mock private DocumentEventPublisher eventPublisher;

    @InjectMocks
    private DocumentRetentionPolicyServiceImpl service;

    private DocumentRetentionPolicy policy;

    @BeforeEach
    void setUp() {
        policy = DocumentTestFixtures.retentionPolicy(DocumentTestFixtures.POLICY_ID);
    }

    @Test
    void shouldCreatePolicyAndPublishEvent() {
        CreateRetentionPolicyRequest request = DocumentTestFixtures.createRetentionPolicyRequest();
        when(retentionPolicyMapper.toEntity(request)).thenReturn(policy);
        when(retentionPolicyRepository.findByOrganizationIdAndNameAndDeletedFalse(
                DocumentTestFixtures.ORG_ID, "Default Policy")).thenReturn(Optional.empty());
        when(retentionPolicyRepository.save(policy)).thenReturn(policy);

        DocumentRetentionPolicy saved = service.createPolicy(request);

        assertThat(saved).isSameAs(policy);
        verify(retentionPolicyValidator).validateCreate(request);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldRejectCreateWhenDuplicateNameExists() {
        CreateRetentionPolicyRequest request = DocumentTestFixtures.createRetentionPolicyRequest();
        when(retentionPolicyRepository.findByOrganizationIdAndNameAndDeletedFalse(
                DocumentTestFixtures.ORG_ID, "Default Policy")).thenReturn(Optional.of(policy));

        assertThatThrownBy(() -> service.createPolicy(request))
                .isInstanceOf(DocumentValidationException.class);
        verify(retentionPolicyRepository, never()).save(any());
    }

    @Test
    void shouldUpdatePolicyAndPublishEvent() {
        UpdateRetentionPolicyRequest request = new UpdateRetentionPolicyRequest(
                "Updated", 180, RetentionAction.DELETE, false, "Desc", true, 0L);
        when(retentionPolicyRepository.findByIdAndDeletedFalse(DocumentTestFixtures.POLICY_ID))
                .thenReturn(Optional.of(policy));
        when(retentionPolicyRepository.save(policy)).thenReturn(policy);

        DocumentRetentionPolicy saved = service.updatePolicy(DocumentTestFixtures.POLICY_ID, request);

        assertThat(saved).isSameAs(policy);
        verify(retentionPolicyValidator).validateUpdate(request);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldDeletePolicyAndPublishEvent() {
        when(retentionPolicyRepository.findByIdAndDeletedFalse(DocumentTestFixtures.POLICY_ID))
                .thenReturn(Optional.of(policy));
        when(retentionPolicyRepository.save(policy)).thenReturn(policy);

        service.deletePolicy(DocumentTestFixtures.POLICY_ID);

        assertThat(policy.getDeleted()).isTrue();
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldRejectDeleteWhenLegalHoldActive() {
        policy.setLegalHold(true);
        when(retentionPolicyRepository.findByIdAndDeletedFalse(DocumentTestFixtures.POLICY_ID))
                .thenReturn(Optional.of(policy));

        assertThatThrownBy(() -> service.deletePolicy(DocumentTestFixtures.POLICY_ID))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldRestoreDeletedPolicy() {
        policy.setDeleted(true);
        when(retentionPolicyRepository.findById(DocumentTestFixtures.POLICY_ID)).thenReturn(Optional.of(policy));
        when(retentionPolicyRepository.save(policy)).thenReturn(policy);

        DocumentRetentionPolicy saved = service.restorePolicy(DocumentTestFixtures.POLICY_ID);

        assertThat(saved.getDeleted()).isFalse();
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldThrowWhenPolicyNotFound() {
        when(retentionPolicyRepository.findByIdAndDeletedFalse(DocumentTestFixtures.POLICY_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findPolicy(DocumentTestFixtures.POLICY_ID))
                .isInstanceOf(RetentionPolicyNotFoundException.class);
    }
}

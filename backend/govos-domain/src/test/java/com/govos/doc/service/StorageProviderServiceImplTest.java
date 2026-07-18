package com.govos.doc.service.impl;

import com.govos.doc.dto.storage.CreateStorageProviderRequest;
import com.govos.doc.dto.storage.UpdateStorageProviderRequest;
import com.govos.doc.entity.StorageProvider;
import com.govos.doc.enums.StorageProviderType;
import com.govos.doc.event.publisher.DocumentEventPublisher;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.exception.StorageProviderNotFoundException;
import com.govos.doc.mapper.StorageProviderMapper;
import com.govos.doc.repository.StorageProviderRepository;
import com.govos.doc.support.DocumentTestFixtures;
import com.govos.doc.validator.StorageProviderValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageProviderServiceImplTest {

    @Mock private StorageProviderRepository storageProviderRepository;
    @Mock private StorageProviderMapper storageProviderMapper;
    @Mock private StorageProviderValidator storageProviderValidator;
    @Mock private DocumentEventPublisher eventPublisher;

    @InjectMocks
    private StorageProviderServiceImpl service;

    private StorageProvider provider;

    @BeforeEach
    void setUp() {
        provider = DocumentTestFixtures.storageProvider(DocumentTestFixtures.PROVIDER_ID);
    }

    @Test
    void shouldCreateProviderAndPublishEvent() {
        CreateStorageProviderRequest request = DocumentTestFixtures.createStorageProviderRequest();
        when(storageProviderRepository.findByProviderNameAndDeletedFalse("minio-primary"))
                .thenReturn(Optional.empty());
        when(storageProviderMapper.toEntity(request)).thenReturn(provider);
        when(storageProviderRepository.save(provider)).thenReturn(provider);

        StorageProvider saved = service.createProvider(request);

        assertThat(saved).isSameAs(provider);
        verify(storageProviderValidator).validateCreate(request);
        verify(eventPublisher, org.mockito.Mockito.atLeastOnce()).publish(any());
    }

    @Test
    void shouldRejectCreateWhenDuplicateProviderNameExists() {
        CreateStorageProviderRequest request = DocumentTestFixtures.createStorageProviderRequest();
        when(storageProviderRepository.findByProviderNameAndDeletedFalse("minio-primary"))
                .thenReturn(Optional.of(provider));

        assertThatThrownBy(() -> service.createProvider(request))
                .isInstanceOf(DocumentValidationException.class);
        verify(storageProviderRepository, never()).save(any());
    }

    @Test
    void shouldUpdateProviderAndPublishEvent() {
        UpdateStorageProviderRequest request = new UpdateStorageProviderRequest(
                "updated", StorageProviderType.MINIO, "bucket", "http://localhost:9000",
                "local", true, false, "secret", true, 0L);
        when(storageProviderRepository.findByIdAndDeletedFalse(DocumentTestFixtures.PROVIDER_ID))
                .thenReturn(Optional.of(provider));
        when(storageProviderRepository.save(provider)).thenReturn(provider);

        StorageProvider saved = service.updateProvider(DocumentTestFixtures.PROVIDER_ID, request);

        assertThat(saved).isSameAs(provider);
        verify(storageProviderValidator).validateUpdate(request);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldActivateProviderAndPublishEvent() {
        provider.setActive(false);
        when(storageProviderRepository.findByIdAndDeletedFalse(DocumentTestFixtures.PROVIDER_ID))
                .thenReturn(Optional.of(provider));
        when(storageProviderRepository.save(provider)).thenReturn(provider);

        StorageProvider saved = service.activateProvider(DocumentTestFixtures.PROVIDER_ID);

        assertThat(saved.getActive()).isTrue();
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldRejectDeactivateWhenProviderIsDefault() {
        when(storageProviderRepository.findByIdAndDeletedFalse(DocumentTestFixtures.PROVIDER_ID))
                .thenReturn(Optional.of(provider));

        assertThatThrownBy(() -> service.deactivateProvider(DocumentTestFixtures.PROVIDER_ID))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldSetDefaultProviderAndPublishEvent() {
        provider.setIsDefault(false);
        when(storageProviderRepository.findByIdAndDeletedFalse(DocumentTestFixtures.PROVIDER_ID))
                .thenReturn(Optional.of(provider));
        when(storageProviderRepository.findByIsDefaultTrueAndDeletedFalse()).thenReturn(Optional.empty());
        when(storageProviderRepository.findByActiveTrueAndDeletedFalse()).thenReturn(List.of());
        when(storageProviderRepository.save(provider)).thenReturn(provider);

        StorageProvider saved = service.setDefaultProvider(DocumentTestFixtures.PROVIDER_ID);

        assertThat(saved.getIsDefault()).isTrue();
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldDeactivateNonDefaultProviderAndPublishEvent() {
        provider.setIsDefault(false);
        when(storageProviderRepository.findByIdAndDeletedFalse(DocumentTestFixtures.PROVIDER_ID))
                .thenReturn(Optional.of(provider));
        when(storageProviderRepository.save(provider)).thenReturn(provider);

        StorageProvider saved = service.deactivateProvider(DocumentTestFixtures.PROVIDER_ID);

        assertThat(saved.getActive()).isFalse();
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldCreateDefaultProviderAndPublishDefaultChangedEvent() {
        CreateStorageProviderRequest base = DocumentTestFixtures.createStorageProviderRequest();
        CreateStorageProviderRequest request = new CreateStorageProviderRequest(
                base.code(), base.providerName(), base.providerType(), base.bucketName(),
                base.endpoint(), base.region(), base.encryptionEnabled(), true,
                base.secretKeyReference(), base.active());
        provider.setIsDefault(true);
        when(storageProviderRepository.findByProviderNameAndDeletedFalse("minio-primary"))
                .thenReturn(Optional.empty());
        when(storageProviderMapper.toEntity(request)).thenReturn(provider);
        when(storageProviderRepository.findByIsDefaultTrueAndDeletedFalse()).thenReturn(Optional.empty());
        when(storageProviderRepository.save(provider)).thenReturn(provider);

        service.createProvider(request);

        verify(eventPublisher, org.mockito.Mockito.atLeast(2)).publish(any());
    }

    @Test
    void shouldThrowWhenProviderNotFound() {
        when(storageProviderRepository.findByIdAndDeletedFalse(DocumentTestFixtures.PROVIDER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findProvider(DocumentTestFixtures.PROVIDER_ID))
                .isInstanceOf(StorageProviderNotFoundException.class);
    }

    @Test
    void shouldClearExistingDefaultWhenCreatingNewDefaultProvider() {
        StorageProvider existingDefault = DocumentTestFixtures.storageProvider(UUID.randomUUID());
        existingDefault.setIsDefault(true);
        CreateStorageProviderRequest base = DocumentTestFixtures.createStorageProviderRequest();
        CreateStorageProviderRequest request = new CreateStorageProviderRequest(
                base.code(), base.providerName(), base.providerType(), base.bucketName(),
                base.endpoint(), base.region(), base.encryptionEnabled(), true,
                base.secretKeyReference(), base.active());
        provider.setIsDefault(true);
        when(storageProviderRepository.findByProviderNameAndDeletedFalse("minio-primary"))
                .thenReturn(Optional.empty());
        when(storageProviderMapper.toEntity(request)).thenReturn(provider);
        when(storageProviderRepository.findByIsDefaultTrueAndDeletedFalse())
                .thenReturn(Optional.of(existingDefault));
        when(storageProviderRepository.findByActiveTrueAndDeletedFalse()).thenReturn(List.of(existingDefault));
        when(storageProviderRepository.save(existingDefault)).thenReturn(existingDefault);
        when(storageProviderRepository.save(provider)).thenReturn(provider);

        service.createProvider(request);

        assertThat(existingDefault.getIsDefault()).isFalse();
    }

    @Test
    void shouldRejectSetDefaultWhenProviderInactive() {
        provider.setActive(false);
        provider.setIsDefault(false);
        when(storageProviderRepository.findByIdAndDeletedFalse(DocumentTestFixtures.PROVIDER_ID))
                .thenReturn(Optional.of(provider));

        assertThatThrownBy(() -> service.setDefaultProvider(DocumentTestFixtures.PROVIDER_ID))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldUpdateProviderAsDefaultAndPublishDefaultChangedEvent() {
        StorageProvider existingDefault = DocumentTestFixtures.storageProvider(UUID.randomUUID());
        existingDefault.setIsDefault(true);
        provider.setIsDefault(false);
        UpdateStorageProviderRequest request = new UpdateStorageProviderRequest(
                null, null, null, null, null, null, true, null, null, 0L);
        when(storageProviderRepository.findByIdAndDeletedFalse(DocumentTestFixtures.PROVIDER_ID))
                .thenReturn(Optional.of(provider));
        when(storageProviderRepository.findByIsDefaultTrueAndDeletedFalse())
                .thenReturn(Optional.of(existingDefault));
        when(storageProviderRepository.findByActiveTrueAndDeletedFalse()).thenReturn(List.of(existingDefault));
        when(storageProviderRepository.save(existingDefault)).thenReturn(existingDefault);
        when(storageProviderRepository.save(provider)).thenReturn(provider);

        StorageProvider saved = service.updateProvider(DocumentTestFixtures.PROVIDER_ID, request);

        assertThat(saved.getIsDefault()).isTrue();
        verify(eventPublisher, org.mockito.Mockito.atLeast(2)).publish(any());
    }

    @Test
    void shouldRejectUpdateWhenVersionConflict() {
        provider.setVersion(3L);
        UpdateStorageProviderRequest request = new UpdateStorageProviderRequest(
                null, null, null, null, null, null, null, null, null, 0L);
        when(storageProviderRepository.findByIdAndDeletedFalse(DocumentTestFixtures.PROVIDER_ID))
                .thenReturn(Optional.of(provider));

        assertThatThrownBy(() -> service.updateProvider(DocumentTestFixtures.PROVIDER_ID, request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldUnsetDefaultFlagOnUpdate() {
        UpdateStorageProviderRequest request = new UpdateStorageProviderRequest(
                null, null, null, null, null, null, false, null, null, 0L);
        when(storageProviderRepository.findByIdAndDeletedFalse(DocumentTestFixtures.PROVIDER_ID))
                .thenReturn(Optional.of(provider));
        when(storageProviderRepository.save(provider)).thenReturn(provider);

        StorageProvider saved = service.updateProvider(DocumentTestFixtures.PROVIDER_ID, request);

        assertThat(saved.getIsDefault()).isFalse();
    }
}

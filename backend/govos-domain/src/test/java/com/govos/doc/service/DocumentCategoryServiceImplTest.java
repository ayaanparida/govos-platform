package com.govos.doc.service.impl;

import com.govos.doc.dto.category.CreateDocumentCategoryRequest;
import com.govos.doc.dto.category.UpdateDocumentCategoryRequest;
import com.govos.doc.entity.DocumentCategory;
import com.govos.doc.entity.DocumentRetentionPolicy;
import com.govos.doc.event.publisher.DocumentEventPublisher;
import com.govos.doc.exception.CategoryNotFoundException;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.exception.RetentionPolicyNotFoundException;
import com.govos.doc.mapper.DocumentCategoryMapper;
import com.govos.doc.repository.DocumentCategoryRepository;
import com.govos.doc.repository.DocumentRetentionPolicyRepository;
import com.govos.doc.support.DocumentTestFixtures;
import com.govos.doc.validator.DocumentCategoryValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentCategoryServiceImplTest {

    @Mock private DocumentCategoryRepository categoryRepository;
    @Mock private DocumentRetentionPolicyRepository retentionPolicyRepository;
    @Mock private DocumentCategoryMapper categoryMapper;
    @Mock private DocumentCategoryValidator categoryValidator;
    @Mock private DocumentEventPublisher eventPublisher;

    @InjectMocks
    private DocumentCategoryServiceImpl service;

    private DocumentCategory category;

    @BeforeEach
    void setUp() {
        category = DocumentTestFixtures.category(DocumentTestFixtures.CATEGORY_ID);
    }

    @Test
    void shouldCreateCategoryAndPublishEvent() {
        CreateDocumentCategoryRequest request = DocumentTestFixtures.createCategoryRequest();
        when(categoryMapper.toEntity(request)).thenReturn(category);
        when(categoryRepository.findByOrganizationIdAndCodeAndDeletedFalse(
                DocumentTestFixtures.ORG_ID, "CAT-001")).thenReturn(Optional.empty());
        when(categoryRepository.save(category)).thenReturn(category);

        DocumentCategory saved = service.createCategory(request);

        assertThat(saved).isSameAs(category);
        assertThat(saved.getCode()).isEqualTo("CAT-001");
        verify(categoryValidator).validateCreate(request);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldRejectCreateWhenDuplicateCodeExists() {
        CreateDocumentCategoryRequest request = DocumentTestFixtures.createCategoryRequest();
        when(categoryRepository.findByOrganizationIdAndCodeAndDeletedFalse(
                DocumentTestFixtures.ORG_ID, "CAT-001")).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> service.createCategory(request))
                .isInstanceOf(DocumentValidationException.class);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldUpdateCategoryAndPublishEvent() {
        UpdateDocumentCategoryRequest request = new UpdateDocumentCategoryRequest(
                "Updated", null, null, null, null, true, 0L);
        when(categoryRepository.findByIdAndDeletedFalse(DocumentTestFixtures.CATEGORY_ID))
                .thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);

        DocumentCategory saved = service.updateCategory(DocumentTestFixtures.CATEGORY_ID, request);

        assertThat(saved).isSameAs(category);
        verify(categoryValidator).validateUpdate(request, DocumentTestFixtures.CATEGORY_ID);
        verify(categoryMapper).updateEntity(request, category);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldDeleteCategoryAndPublishEvent() {
        when(categoryRepository.findByIdAndDeletedFalse(DocumentTestFixtures.CATEGORY_ID))
                .thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);

        service.deleteCategory(DocumentTestFixtures.CATEGORY_ID);

        assertThat(category.getDeleted()).isTrue();
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldRestoreDeletedCategory() {
        category.setDeleted(true);
        when(categoryRepository.findById(DocumentTestFixtures.CATEGORY_ID)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);

        DocumentCategory saved = service.restoreCategory(DocumentTestFixtures.CATEGORY_ID);

        assertThat(saved.getDeleted()).isFalse();
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldThrowWhenCategoryNotFound() {
        when(categoryRepository.findByIdAndDeletedFalse(DocumentTestFixtures.CATEGORY_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCategory(
                DocumentTestFixtures.CATEGORY_ID,
                new UpdateDocumentCategoryRequest("Updated", null, null, null, null, true, 0L)))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void shouldCreateCategoryWithParentAndRetentionPolicy() {
        DocumentCategory parent = DocumentTestFixtures.category(DocumentTestFixtures.FOLDER_ID);
        DocumentRetentionPolicy policy = DocumentTestFixtures.retentionPolicy(DocumentTestFixtures.POLICY_ID);
        CreateDocumentCategoryRequest request = new CreateDocumentCategoryRequest(
                "CAT-002", "Child", DocumentTestFixtures.ORG_ID, DocumentTestFixtures.FOLDER_ID,
                DocumentTestFixtures.POLICY_ID, null, null, true);
        when(categoryRepository.findByOrganizationIdAndCodeAndDeletedFalse(
                DocumentTestFixtures.ORG_ID, "CAT-002")).thenReturn(Optional.empty());
        when(categoryRepository.findByIdAndDeletedFalse(DocumentTestFixtures.FOLDER_ID))
                .thenReturn(Optional.of(parent));
        when(retentionPolicyRepository.findByIdAndDeletedFalse(DocumentTestFixtures.POLICY_ID))
                .thenReturn(Optional.of(policy));
        when(categoryMapper.toEntity(request)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);

        DocumentCategory saved = service.createCategory(request);

        assertThat(saved.getParentCategory()).isSameAs(parent);
        assertThat(saved.getDefaultRetentionPolicy()).isSameAs(policy);
    }

    @Test
    void shouldRejectCreateWhenParentBelongsToDifferentOrganization() {
        DocumentCategory parent = DocumentTestFixtures.category(DocumentTestFixtures.FOLDER_ID);
        parent.setOrganizationId(UUID.randomUUID());
        CreateDocumentCategoryRequest request = new CreateDocumentCategoryRequest(
                "CAT-002", "Child", DocumentTestFixtures.ORG_ID, DocumentTestFixtures.FOLDER_ID,
                null, null, null, true);
        when(categoryRepository.findByOrganizationIdAndCodeAndDeletedFalse(
                DocumentTestFixtures.ORG_ID, "CAT-002")).thenReturn(Optional.empty());
        when(categoryRepository.findByIdAndDeletedFalse(DocumentTestFixtures.FOLDER_ID))
                .thenReturn(Optional.of(parent));
        when(categoryMapper.toEntity(request)).thenReturn(category);

        assertThatThrownBy(() -> service.createCategory(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldRejectUpdateWhenRetentionPolicyNotFound() {
        UpdateDocumentCategoryRequest request = new UpdateDocumentCategoryRequest(
                null, null, DocumentTestFixtures.POLICY_ID, null, null, null, 0L);
        when(categoryRepository.findByIdAndDeletedFalse(DocumentTestFixtures.CATEGORY_ID))
                .thenReturn(Optional.of(category));
        when(retentionPolicyRepository.findByIdAndDeletedFalse(DocumentTestFixtures.POLICY_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCategory(DocumentTestFixtures.CATEGORY_ID, request))
                .isInstanceOf(RetentionPolicyNotFoundException.class);
    }

    @Test
    void shouldRejectUpdateWhenVersionConflict() {
        category.setVersion(5L);
        UpdateDocumentCategoryRequest request = new UpdateDocumentCategoryRequest(
                "Updated", null, null, null, null, null, 0L);
        when(categoryRepository.findByIdAndDeletedFalse(DocumentTestFixtures.CATEGORY_ID))
                .thenReturn(Optional.of(category));

        assertThatThrownBy(() -> service.updateCategory(DocumentTestFixtures.CATEGORY_ID, request))
                .isInstanceOf(DocumentValidationException.class);
    }
}

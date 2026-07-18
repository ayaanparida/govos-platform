package com.govos.doc.service.impl;

import com.govos.doc.dto.folder.CreateFolderRequest;
import com.govos.doc.entity.Folder;
import com.govos.doc.event.publisher.DocumentEventPublisher;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.exception.FolderNotFoundException;
import com.govos.doc.mapper.FolderMapper;
import com.govos.doc.repository.FolderRepository;
import com.govos.doc.support.DocumentTestFixtures;
import com.govos.doc.validator.FolderValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FolderServiceImplTest {

    @Mock private FolderRepository folderRepository;
    @Mock private FolderMapper folderMapper;
    @Mock private FolderValidator folderValidator;
    @Mock private DocumentEventPublisher eventPublisher;

    @InjectMocks
    private FolderServiceImpl service;

    private Folder folder;

    @BeforeEach
    void setUp() {
        folder = DocumentTestFixtures.folder(DocumentTestFixtures.FOLDER_ID);
    }

    @Test
    void shouldCreateFolderAndPublishEvent() {
        CreateFolderRequest request = DocumentTestFixtures.createFolderRequest();
        when(folderMapper.toEntity(request)).thenReturn(folder);
        when(folderRepository.findByOrganizationIdAndDeletedFalse(DocumentTestFixtures.ORG_ID))
                .thenReturn(List.of());
        when(folderRepository.save(folder)).thenReturn(folder);

        Folder saved = service.createFolder(request);

        assertThat(saved).isSameAs(folder);
        assertThat(saved.getDeleted()).isFalse();
        verify(folderValidator).validateCreate(request);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldRejectCreateWhenDuplicateNameAtLevel() {
        CreateFolderRequest request = DocumentTestFixtures.createFolderRequest();
        Folder duplicate = DocumentTestFixtures.folder(DocumentTestFixtures.FOLDER_ID);
        duplicate.setName("Folder");
        when(folderRepository.findByOrganizationIdAndDeletedFalse(DocumentTestFixtures.ORG_ID))
                .thenReturn(List.of(duplicate));

        assertThatThrownBy(() -> service.createFolder(request))
                .isInstanceOf(DocumentValidationException.class);
        verify(folderRepository, never()).save(any());
    }

    @Test
    void shouldRenameFolderAndPublishEvent() {
        when(folderRepository.findByIdAndDeletedFalse(DocumentTestFixtures.FOLDER_ID))
                .thenReturn(Optional.of(folder));
        when(folderRepository.findByOrganizationIdAndDeletedFalse(DocumentTestFixtures.ORG_ID))
                .thenReturn(List.of(folder));
        when(folderRepository.save(folder)).thenReturn(folder);

        Folder saved = service.renameFolder(DocumentTestFixtures.FOLDER_ID, "Renamed", 0L);

        assertThat(saved.getName()).isEqualTo("Renamed");
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldDeleteFolderWhenNoChildren() {
        when(folderRepository.findByIdAndDeletedFalse(DocumentTestFixtures.FOLDER_ID))
                .thenReturn(Optional.of(folder));
        when(folderRepository.findByParentFolder_IdAndDeletedFalse(DocumentTestFixtures.FOLDER_ID))
                .thenReturn(List.of());
        when(folderRepository.save(folder)).thenReturn(folder);

        service.deleteFolder(DocumentTestFixtures.FOLDER_ID);

        assertThat(folder.getDeleted()).isTrue();
        verify(folderValidator).validateDelete(DocumentTestFixtures.FOLDER_ID);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldRejectDeleteWhenFolderHasChildren() {
        when(folderRepository.findByIdAndDeletedFalse(DocumentTestFixtures.FOLDER_ID))
                .thenReturn(Optional.of(folder));
        when(folderRepository.findByParentFolder_IdAndDeletedFalse(DocumentTestFixtures.FOLDER_ID))
                .thenReturn(List.of(DocumentTestFixtures.folder(DocumentTestFixtures.CATEGORY_ID)));

        assertThatThrownBy(() -> service.deleteFolder(DocumentTestFixtures.FOLDER_ID))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldRestoreDeletedFolder() {
        folder.setDeleted(true);
        when(folderRepository.findById(DocumentTestFixtures.FOLDER_ID)).thenReturn(Optional.of(folder));
        when(folderRepository.save(folder)).thenReturn(folder);

        Folder saved = service.restoreFolder(DocumentTestFixtures.FOLDER_ID);

        assertThat(saved.getDeleted()).isFalse();
        assertThat(saved.getActive()).isTrue();
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldMoveFolderAndPublishEvent() {
        Folder parent = DocumentTestFixtures.folder(DocumentTestFixtures.CATEGORY_ID);
        when(folderRepository.findByIdAndDeletedFalse(DocumentTestFixtures.FOLDER_ID))
                .thenReturn(Optional.of(folder));
        when(folderRepository.findByIdAndDeletedFalse(DocumentTestFixtures.CATEGORY_ID))
                .thenReturn(Optional.of(parent));
        when(folderRepository.findByParentFolder_IdAndDeletedFalse(DocumentTestFixtures.CATEGORY_ID))
                .thenReturn(List.of());
        when(folderRepository.save(folder)).thenReturn(folder);

        Folder saved = service.moveFolder(DocumentTestFixtures.FOLDER_ID, DocumentTestFixtures.CATEGORY_ID, 0L);

        assertThat(saved.getParentFolder()).isSameAs(parent);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldThrowWhenFolderNotFound() {
        when(folderRepository.findByIdAndDeletedFalse(DocumentTestFixtures.FOLDER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findFolder(DocumentTestFixtures.FOLDER_ID))
                .isInstanceOf(FolderNotFoundException.class);
    }
}

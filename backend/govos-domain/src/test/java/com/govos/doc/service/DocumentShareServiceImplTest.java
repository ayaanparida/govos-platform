package com.govos.doc.service.impl;

import com.govos.doc.dto.share.CreateShareRequest;
import com.govos.doc.entity.Document;
import com.govos.doc.entity.DocumentShare;
import com.govos.doc.enums.ShareType;
import com.govos.doc.event.publisher.DocumentEventPublisher;
import com.govos.doc.exception.DocumentNotFoundException;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.exception.ShareNotFoundException;
import com.govos.doc.mapper.DocumentShareMapper;
import com.govos.doc.repository.DocumentRepository;
import com.govos.doc.repository.DocumentShareRepository;
import com.govos.doc.support.DocumentTestFixtures;
import com.govos.doc.validator.DocumentShareValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentShareServiceImplTest {

    @Mock private DocumentShareRepository shareRepository;
    @Mock private DocumentRepository documentRepository;
    @Mock private DocumentShareMapper shareMapper;
    @Mock private DocumentShareValidator shareValidator;
    @Mock private DocumentEventPublisher eventPublisher;

    @InjectMocks
    private DocumentShareServiceImpl service;

    private Document document;
    private DocumentShare share;

    @BeforeEach
    void setUp() {
        document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        share = DocumentTestFixtures.share(DocumentTestFixtures.SHARE_ID, document);
    }

    @Test
    void shouldCreateShareAndPublishEvent() {
        CreateShareRequest request = DocumentTestFixtures.createShareRequest(DocumentTestFixtures.DOCUMENT_ID);
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));
        when(shareRepository.findByDocument_IdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(List.of());
        when(shareMapper.toEntity(request)).thenReturn(share);
        when(shareRepository.save(share)).thenReturn(share);

        DocumentShare saved = service.createShare(request);

        assertThat(saved).isSameAs(share);
        verify(shareValidator).validateCreate(request);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldRejectCreateWhenDuplicateShareExists() {
        CreateShareRequest request = DocumentTestFixtures.createShareRequest(DocumentTestFixtures.DOCUMENT_ID);
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));
        when(shareRepository.findByDocument_IdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(List.of(share));

        assertThatThrownBy(() -> service.createShare(request))
                .isInstanceOf(DocumentValidationException.class);
        verify(shareRepository, never()).save(any());
    }

    @Test
    void shouldRejectCreateWhenDocumentNotFound() {
        CreateShareRequest request = DocumentTestFixtures.createShareRequest(DocumentTestFixtures.DOCUMENT_ID);
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createShare(request))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    void shouldExpireShareAndPublishEvent() {
        when(shareRepository.findByIdAndDeletedFalse(DocumentTestFixtures.SHARE_ID))
                .thenReturn(Optional.of(share));
        when(shareRepository.save(share)).thenReturn(share);

        DocumentShare saved = service.expireShare(DocumentTestFixtures.SHARE_ID);

        assertThat(saved.getActive()).isFalse();
        assertThat(saved.getExpiresAt()).isBeforeOrEqualTo(Instant.now());
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldRevokeShareAndPublishEvent() {
        when(shareRepository.findByIdAndDeletedFalse(DocumentTestFixtures.SHARE_ID))
                .thenReturn(Optional.of(share));
        when(shareRepository.save(share)).thenReturn(share);

        DocumentShare saved = service.revokeShare(DocumentTestFixtures.SHARE_ID);

        assertThat(saved.getDeleted()).isTrue();
        assertThat(saved.getActive()).isFalse();
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldThrowWhenShareNotFound() {
        when(shareRepository.findByIdAndDeletedFalse(DocumentTestFixtures.SHARE_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findShare(DocumentTestFixtures.SHARE_ID))
                .isInstanceOf(ShareNotFoundException.class);
    }

    @Test
    void shouldNotDetectDuplicateForDifferentShareType() {
        CreateShareRequest request = new CreateShareRequest(
                DocumentTestFixtures.DOCUMENT_ID, ShareType.ROLE, null,
                DocumentTestFixtures.USER_ID, null, DocumentTestFixtures.OWNER_ID,
                null, "READ", null, null);
        share.setShareType(ShareType.USER);
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));
        when(shareRepository.findByDocument_IdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(List.of(share));
        when(shareMapper.toEntity(request)).thenReturn(share);
        when(shareRepository.save(share)).thenReturn(share);

        assertThat(service.createShare(request)).isSameAs(share);
    }

    @Test
    void shouldRejectCreateWhenDuplicateRoleShareExists() {
        CreateShareRequest request = new CreateShareRequest(
                DocumentTestFixtures.DOCUMENT_ID, ShareType.ROLE, null,
                DocumentTestFixtures.USER_ID, null, DocumentTestFixtures.OWNER_ID,
                null, "READ", null, null);
        share.setShareType(ShareType.ROLE);
        share.setSharedWithRoleId(DocumentTestFixtures.USER_ID);
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));
        when(shareRepository.findByDocument_IdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(List.of(share));

        assertThatThrownBy(() -> service.createShare(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldRejectCreateWhenDuplicatePublicLinkExists() {
        CreateShareRequest request = new CreateShareRequest(
                DocumentTestFixtures.DOCUMENT_ID, ShareType.PUBLIC_LINK, null, null, null,
                DocumentTestFixtures.OWNER_ID, null, "READ", "https://example.com/share", null);
        share.setShareType(ShareType.PUBLIC_LINK);
        share.getShareToken().setPublicLinkUrl("https://example.com/share");
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));
        when(shareRepository.findByDocument_IdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(List.of(share));

        assertThatThrownBy(() -> service.createShare(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldRejectCreateWhenDuplicateSignedUrlExists() {
        Instant expiry = Instant.parse("2099-06-01T00:00:00Z");
        CreateShareRequest request = new CreateShareRequest(
                DocumentTestFixtures.DOCUMENT_ID, ShareType.SIGNED_URL, null, null, null,
                DocumentTestFixtures.OWNER_ID, null, "READ", null, expiry);
        share.setShareType(ShareType.SIGNED_URL);
        share.getShareToken().setSignedUrlExpiresAt(expiry);
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));
        when(shareRepository.findByDocument_IdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(List.of(share));

        assertThatThrownBy(() -> service.createShare(request))
                .isInstanceOf(DocumentValidationException.class);
    }
}

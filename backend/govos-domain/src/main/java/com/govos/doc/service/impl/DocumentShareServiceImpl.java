package com.govos.doc.service.impl;

import com.govos.doc.dto.share.CreateShareRequest;
import com.govos.doc.entity.Document;
import com.govos.doc.entity.DocumentShare;
import com.govos.doc.enums.ShareType;
import com.govos.doc.event.DocumentEvents;
import com.govos.doc.event.publisher.DocumentEventPublisher;
import com.govos.doc.exception.DocumentNotFoundException;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.exception.ShareNotFoundException;
import com.govos.doc.exception.ValidationResult;
import com.govos.doc.mapper.DocumentShareMapper;
import com.govos.doc.repository.DocumentRepository;
import com.govos.doc.repository.DocumentShareRepository;
import com.govos.doc.service.DocumentShareService;
import com.govos.doc.validator.DocumentShareValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DocumentShareServiceImpl implements DocumentShareService {

    private final DocumentShareRepository shareRepository;
    private final DocumentRepository documentRepository;
    private final DocumentShareMapper shareMapper;
    private final DocumentShareValidator shareValidator;
    private final DocumentEventPublisher eventPublisher;

    public DocumentShareServiceImpl(
            DocumentShareRepository shareRepository,
            DocumentRepository documentRepository,
            DocumentShareMapper shareMapper,
            DocumentShareValidator shareValidator,
            DocumentEventPublisher eventPublisher) {
        this.shareRepository = shareRepository;
        this.documentRepository = documentRepository;
        this.shareMapper = shareMapper;
        this.shareValidator = shareValidator;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public DocumentShare createShare(CreateShareRequest request) {
        shareValidator.validateCreate(request);
        Document document = documentRepository.findByIdAndDeletedFalse(request.documentId())
                .orElseThrow(() -> new DocumentNotFoundException(request.documentId()));
        assertNoDuplicateShare(document.getId(), request);

        DocumentShare entity = shareMapper.toEntity(request);
        entity.setDocument(document);
        entity.setDeleted(false);
        entity.setActive(true);
        DocumentShare saved = shareRepository.save(entity);
        eventPublisher.publish(DocumentEvents.documentShared(saved));
        return saved;
    }

    @Override
    @Transactional
    public DocumentShare expireShare(UUID id) {
        DocumentShare entity = findActiveById(id);
        entity.setExpiresAt(Instant.now());
        entity.setActive(false);
        DocumentShare saved = shareRepository.save(entity);
        eventPublisher.publish(DocumentEvents.documentShareExpired(saved));
        return saved;
    }

    @Override
    @Transactional
    public DocumentShare revokeShare(UUID id) {
        DocumentShare entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        DocumentShare saved = shareRepository.save(entity);
        eventPublisher.publish(DocumentEvents.documentShareRevoked(saved));
        return saved;
    }

    @Override
    public DocumentShare findShare(UUID id) {
        return findActiveById(id);
    }

    private DocumentShare findActiveById(UUID id) {
        return shareRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ShareNotFoundException(id));
    }

    private void assertNoDuplicateShare(UUID documentId, CreateShareRequest request) {
        List<DocumentShare> existingShares = shareRepository.findByDocument_IdAndDeletedFalse(documentId);
        boolean duplicate = existingShares.stream().anyMatch(share -> matchesShareTarget(share, request));
        if (duplicate) {
            ValidationResult result = new ValidationResult();
            result.addError(
                    "shareType",
                    "An active share already exists for this recipient and document",
                    "DOC_DUPLICATE_SHARE");
            result.throwIfInvalid();
        }
    }

    private boolean matchesShareTarget(DocumentShare share, CreateShareRequest request) {
        if (share.getShareType() != request.shareType()) {
            return false;
        }
        return switch (request.shareType()) {
            case USER -> request.sharedWithUserId() != null
                    && request.sharedWithUserId().equals(share.getSharedWithUserId());
            case ROLE -> request.sharedWithRoleId() != null
                    && request.sharedWithRoleId().equals(share.getSharedWithRoleId());
            case PUBLIC_LINK -> share.getShareToken() != null
                    && request.publicLinkUrl() != null
                    && request.publicLinkUrl().equals(share.getShareToken().getPublicLinkUrl());
            case SIGNED_URL -> share.getShareToken() != null
                    && request.signedUrlExpiresAt() != null
                    && request.signedUrlExpiresAt().equals(share.getShareToken().getSignedUrlExpiresAt());
        };
    }
}

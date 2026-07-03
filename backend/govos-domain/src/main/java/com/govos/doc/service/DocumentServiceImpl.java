package com.govos.doc.service;

import com.govos.idm.entity.User;
import com.govos.idm.exception.UserNotFoundException;
import com.govos.idm.repository.UserRepository;
import com.govos.doc.dto.CreateDocumentRequest;
import com.govos.doc.dto.DocumentDto;
import com.govos.doc.dto.UpdateDocumentRequest;
import com.govos.doc.entity.Document;
import com.govos.doc.entity.DocumentStatus;
import com.govos.doc.entity.DocumentVisibility;
import com.govos.doc.entity.Folder;
import com.govos.doc.entity.StorageProvider;
import com.govos.doc.exception.DocumentNotFoundException;
import com.govos.doc.exception.FolderNotFoundException;
import com.govos.doc.exception.StorageProviderNotFoundException;
import com.govos.doc.mapper.DocumentMapper;
import com.govos.doc.repository.DocumentRepository;
import com.govos.doc.repository.FolderRepository;
import com.govos.doc.repository.StorageProviderRepository;
import com.govos.doc.validator.DocumentValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final StorageProviderRepository storageProviderRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final DocumentMapper documentMapper;
    private final DocumentValidator documentValidator;

    public DocumentServiceImpl(
            DocumentRepository documentRepository,
            StorageProviderRepository storageProviderRepository,
            FolderRepository folderRepository,
            UserRepository userRepository,
            DocumentMapper documentMapper,
            DocumentValidator documentValidator) {
        this.documentRepository = documentRepository;
        this.storageProviderRepository = storageProviderRepository;
        this.folderRepository = folderRepository;
        this.userRepository = userRepository;
        this.documentMapper = documentMapper;
        this.documentValidator = documentValidator;
    }

    @Override
    public DocumentDto getById(UUID id) {
        return documentMapper.toDto(findActiveById(id));
    }

    @Override
    public DocumentDto getByCode(String code) {
        return documentMapper.toDto(findActiveByCode(code));
    }

    @Override
    public List<DocumentDto> getAll() {
        return documentRepository.findByDeletedFalseOrderByOriginalFilenameAsc().stream()
                .map(documentMapper::toDto)
                .toList();
    }

    @Override
    public List<DocumentDto> getByFolderId(UUID folderId) {
        return documentRepository.findByFolder_IdAndDeletedFalseOrderByOriginalFilenameAsc(folderId).stream()
                .map(documentMapper::toDto)
                .toList();
    }

    @Override
    public List<DocumentDto> getByOwnerId(UUID ownerId) {
        return documentRepository.findByOwner_IdAndDeletedFalseOrderByOriginalFilenameAsc(ownerId).stream()
                .map(documentMapper::toDto)
                .toList();
    }

    @Override
    public List<DocumentDto> getByStatus(DocumentStatus status) {
        return documentRepository.findByStatusAndDeletedFalseOrderByOriginalFilenameAsc(status).stream()
                .map(documentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public DocumentDto create(CreateDocumentRequest request) {
        documentValidator.validateCreate(request);

        Document entity = new Document();
        applyMetadata(entity, request.code(), request.originalFilename(), request.storedFilename(),
                request.mimeType(), request.extension(), request.size(), request.checksum(),
                request.storageProviderId(), request.folderId(), request.ownerId(),
                request.visibility(), request.status());
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return documentMapper.toDto(documentRepository.save(entity));
    }

    @Override
    @Transactional
    public DocumentDto update(UUID id, UpdateDocumentRequest request) {
        Document entity = findActiveById(id);
        assertVersion(entity, request.version());
        documentValidator.validateUpdate(id, request);

        applyMetadata(entity, request.code(), request.originalFilename(), request.storedFilename(),
                request.mimeType(), request.extension(), request.size(), request.checksum(),
                request.storageProviderId(), request.folderId(), request.ownerId(),
                request.visibility(), request.status());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return documentMapper.toDto(documentRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        Document entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        entity.setStatus(DocumentStatus.DELETED);
        documentRepository.save(entity);
    }

    private void applyMetadata(
            Document entity,
            String code,
            String originalFilename,
            String storedFilename,
            String mimeType,
            String extension,
            Long size,
            String checksum,
            UUID storageProviderId,
            UUID folderId,
            UUID ownerId,
            DocumentVisibility visibility,
            DocumentStatus status) {
        entity.setCode(code);
        entity.setOriginalFilename(originalFilename);
        entity.setStoredFilename(storedFilename);
        entity.setMimeType(mimeType);
        entity.setExtension(extension);
        entity.setSize(size);
        entity.setChecksum(checksum);
        entity.setStorageProvider(resolveStorageProvider(storageProviderId));
        entity.setFolder(folderId != null ? resolveFolder(folderId) : null);
        entity.setOwner(resolveUser(ownerId));
        entity.setVisibility(visibility != null ? visibility : DocumentVisibility.PRIVATE);
        entity.setStatus(status != null ? status : DocumentStatus.DRAFT);
    }

    private StorageProvider resolveStorageProvider(UUID id) {
        return storageProviderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new StorageProviderNotFoundException(id));
    }

    private Folder resolveFolder(UUID id) {
        return folderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new FolderNotFoundException(id));
    }

    private User resolveUser(UUID id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private Document findActiveById(UUID id) {
        return documentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));
    }

    private Document findActiveByCode(String code) {
        return documentRepository.findByCodeAndDeletedFalse(code)
                .orElseThrow(() -> new DocumentNotFoundException(code));
    }

    private void assertVersion(Document entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "Document version mismatch for id: " + entity.getId());
        }
    }
}

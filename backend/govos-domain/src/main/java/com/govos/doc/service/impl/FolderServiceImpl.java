package com.govos.doc.service.impl;

import com.govos.doc.dto.folder.CreateFolderRequest;
import com.govos.doc.entity.Folder;
import com.govos.doc.event.DocumentEvents;
import com.govos.doc.event.publisher.DocumentEventPublisher;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.exception.FolderNotFoundException;
import com.govos.doc.exception.ValidationResult;
import com.govos.doc.mapper.FolderMapper;
import com.govos.doc.repository.FolderRepository;
import com.govos.doc.service.FolderService;
import com.govos.doc.validator.FolderValidator;
import com.govos.doc.validator.ValidationUtils;
import com.govos.doc.valueobject.DocumentPath;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final FolderMapper folderMapper;
    private final FolderValidator folderValidator;
    private final DocumentEventPublisher eventPublisher;

    public FolderServiceImpl(
            FolderRepository folderRepository,
            FolderMapper folderMapper,
            FolderValidator folderValidator,
            DocumentEventPublisher eventPublisher) {
        this.folderRepository = folderRepository;
        this.folderMapper = folderMapper;
        this.folderValidator = folderValidator;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Folder createFolder(CreateFolderRequest request) {
        folderValidator.validateCreate(request);
        Folder parent = resolveParent(request.parentFolderId(), request.organizationId());
        assertDuplicateName(request.organizationId(), parent, request.name(), null);
        assertUniquePath(request.organizationId(), request.materializedPath(), null);

        Folder entity = folderMapper.toEntity(request);
        entity.setDeleted(false);
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setParentFolder(parent);
        applyPathMetadata(entity, parent, request.name(), request.materializedPath(), request.depthLevel());

        Folder saved = folderRepository.save(entity);
        eventPublisher.publish(DocumentEvents.folderCreated(saved));
        return saved;
    }

    @Override
    @Transactional
    public Folder renameFolder(UUID id, String name, Long version) {
        Folder entity = findActiveById(id);
        assertVersion(entity, version);
        ValidationResult result = new ValidationResult();
        ValidationUtils.requireText(result, "name", name);
        ValidationUtils.requireMaxLength(result, "name", name, ValidationUtils.MAX_FOLDER_NAME_LENGTH);
        result.throwIfInvalid();
        assertDuplicateName(entity.getOrganizationId(), entity.getParentFolder(), name, id);
        entity.setName(name);
        applyPathMetadata(entity, entity.getParentFolder(), name, null, null);
        Folder saved = folderRepository.save(entity);
        eventPublisher.publish(DocumentEvents.folderUpdated(saved));
        return saved;
    }

    @Override
    @Transactional
    public Folder moveFolder(UUID id, UUID parentFolderId, Long version) {
        Folder entity = findActiveById(id);
        assertVersion(entity, version);
        folderValidator.validateUpdate(
                new com.govos.doc.dto.folder.UpdateFolderRequest(
                        null, parentFolderId, null, null, null, version),
                id);
        Folder newParent = resolveParent(parentFolderId, entity.getOrganizationId());
        assertNotDescendant(entity, newParent);
        assertDuplicateName(entity.getOrganizationId(), newParent, entity.getName(), id);
        entity.setParentFolder(newParent);
        applyPathMetadata(entity, newParent, entity.getName(), null, null);
        Folder saved = folderRepository.save(entity);
        eventPublisher.publish(DocumentEvents.folderMoved(saved));
        return saved;
    }

    @Override
    @Transactional
    public void deleteFolder(UUID id) {
        Folder entity = findActiveById(id);
        folderValidator.validateDelete(id);
        List<Folder> children = folderRepository.findByParentFolder_IdAndDeletedFalse(id);
        if (!children.isEmpty()) {
            ValidationResult result = new ValidationResult();
            result.addError("folderId", "Cannot delete folder with active child folders", "DOC_FOLDER_NOT_EMPTY");
            result.throwIfInvalid();
        }
        entity.setDeleted(true);
        entity.setActive(false);
        Folder saved = folderRepository.save(entity);
        eventPublisher.publish(DocumentEvents.folderDeleted(saved));
    }

    @Override
    @Transactional
    public Folder restoreFolder(UUID id) {
        Folder entity = folderRepository.findById(id)
                .filter(folder -> Boolean.TRUE.equals(folder.getDeleted()))
                .orElseThrow(() -> new FolderNotFoundException(id));
        entity.setDeleted(false);
        entity.setActive(true);
        Folder saved = folderRepository.save(entity);
        eventPublisher.publish(DocumentEvents.folderRestored(saved));
        return saved;
    }

    @Override
    public Folder findFolder(UUID id) {
        return findActiveById(id);
    }

    private Folder findActiveById(UUID id) {
        return folderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new FolderNotFoundException(id));
    }

    private Folder resolveParent(UUID parentFolderId, UUID organizationId) {
        if (parentFolderId == null) {
            return null;
        }
        Folder parent = findActiveById(parentFolderId);
        if (!organizationId.equals(parent.getOrganizationId())) {
            throw new DocumentValidationException("Parent folder does not belong to organization " + organizationId);
        }
        return parent;
    }

    private void assertDuplicateName(UUID organizationId, Folder parent, String name, UUID excludeId) {
        List<Folder> siblings = parent == null
                ? folderRepository.findByOrganizationIdAndDeletedFalse(organizationId).stream()
                        .filter(folder -> folder.getParentFolder() == null)
                        .toList()
                : folderRepository.findByParentFolder_IdAndDeletedFalse(parent.getId());
        boolean duplicate = siblings.stream()
                .filter(folder -> excludeId == null || !folder.getId().equals(excludeId))
                .anyMatch(folder -> folder.getName().equalsIgnoreCase(name));
        if (duplicate) {
            ValidationResult result = new ValidationResult();
            result.addError("name", "Folder name already exists at this level", "DOC_DUPLICATE_FOLDER_NAME");
            result.throwIfInvalid();
        }
    }

    private void assertUniquePath(UUID organizationId, String path, UUID excludeId) {
        if (!StringUtils.hasText(path)) {
            return;
        }
        folderRepository.findByPathMetadata_MaterializedPathAndDeletedFalse(path)
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .filter(existing -> organizationId.equals(existing.getOrganizationId()))
                .ifPresent(existing -> {
                    ValidationResult result = new ValidationResult();
                    result.addError("materializedPath", "Folder path already exists", "DOC_DUPLICATE_FOLDER_PATH");
                    result.throwIfInvalid();
                });
    }

    private void applyPathMetadata(
            Folder folder,
            Folder parent,
            String name,
            String requestedPath,
            Integer requestedDepth) {
        String path = requestedPath;
        Integer depth = requestedDepth;
        if (!StringUtils.hasText(path)) {
            String parentPath = parent != null && parent.getPathMetadata() != null
                    ? parent.getPathMetadata().getMaterializedPath()
                    : "";
            path = StringUtils.hasText(parentPath) ? parentPath + "/" + name : "/" + name;
        }
        if (depth == null) {
            depth = parent != null && parent.getPathMetadata() != null && parent.getPathMetadata().getDepthLevel() != null
                    ? parent.getPathMetadata().getDepthLevel() + 1
                    : 0;
        }
        ValidationResult result = new ValidationResult();
        if (depth > ValidationUtils.MAX_FOLDER_DEPTH) {
            result.addError(
                    "depthLevel",
                    "Folder hierarchy depth must not exceed " + ValidationUtils.MAX_FOLDER_DEPTH,
                    "DOC_FOLDER_DEPTH_EXCEEDED");
        }
        ValidationUtils.requireMaxLength(result, "materializedPath", path, ValidationUtils.MAX_PATH_LENGTH);
        result.throwIfInvalid();
        folder.setPathMetadata(new DocumentPath(path, depth));
    }

    private void assertNotDescendant(Folder folder, Folder newParent) {
        if (newParent == null) {
            return;
        }
        Folder current = newParent;
        while (current != null) {
            if (folder.getId().equals(current.getId())) {
                throw new DocumentValidationException("Folder cannot be moved under its own descendant");
            }
            current = current.getParentFolder();
        }
    }

    private void assertVersion(Folder entity, Long expectedVersion) {
        if (expectedVersion != null && !expectedVersion.equals(entity.getVersion())) {
            throw new DocumentValidationException("Optimistic lock conflict for folder " + entity.getId());
        }
    }
}

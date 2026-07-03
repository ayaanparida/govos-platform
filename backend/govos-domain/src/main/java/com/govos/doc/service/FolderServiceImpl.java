package com.govos.doc.service;

import com.govos.idm.entity.User;
import com.govos.idm.exception.UserNotFoundException;
import com.govos.idm.repository.UserRepository;
import com.govos.doc.dto.CreateFolderRequest;
import com.govos.doc.dto.FolderDto;
import com.govos.doc.dto.UpdateFolderRequest;
import com.govos.doc.entity.Folder;
import com.govos.doc.exception.FolderNotFoundException;
import com.govos.doc.mapper.FolderMapper;
import com.govos.doc.repository.FolderRepository;
import com.govos.doc.validator.FolderTreeValidator;
import com.govos.doc.validator.FolderValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final FolderMapper folderMapper;
    private final FolderValidator folderValidator;
    private final FolderTreeValidator folderTreeValidator;

    public FolderServiceImpl(
            FolderRepository folderRepository,
            UserRepository userRepository,
            FolderMapper folderMapper,
            FolderValidator folderValidator,
            FolderTreeValidator folderTreeValidator) {
        this.folderRepository = folderRepository;
        this.userRepository = userRepository;
        this.folderMapper = folderMapper;
        this.folderValidator = folderValidator;
        this.folderTreeValidator = folderTreeValidator;
    }

    @Override
    public FolderDto getById(UUID id) {
        return folderMapper.toDto(findActiveById(id));
    }

    @Override
    public FolderDto getByCode(String code) {
        return folderMapper.toDto(findActiveByCode(code));
    }

    @Override
    public List<FolderDto> getAll() {
        return folderRepository.findByDeletedFalseOrderByNameAsc().stream()
                .map(folderMapper::toDto)
                .toList();
    }

    @Override
    public List<FolderDto> getByOwnerId(UUID ownerId) {
        return folderRepository.findByOwner_IdAndDeletedFalseOrderByNameAsc(ownerId).stream()
                .map(folderMapper::toDto)
                .toList();
    }

    @Override
    public List<FolderDto> getByParentFolderId(UUID parentFolderId) {
        return folderRepository.findByParentFolder_IdAndDeletedFalseOrderByNameAsc(parentFolderId).stream()
                .map(folderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public FolderDto create(CreateFolderRequest request) {
        folderValidator.validateCreate(request);

        Folder entity = new Folder();
        entity.setCode(request.code());
        entity.setName(request.name());
        entity.setOwner(resolveUser(request.ownerId()));
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        if (request.parentFolderId() != null) {
            entity.setParentFolder(findActiveById(request.parentFolderId()));
        }

        return folderMapper.toDto(folderRepository.save(entity));
    }

    @Override
    @Transactional
    public FolderDto update(UUID id, UpdateFolderRequest request) {
        Folder entity = findActiveById(id);
        assertVersion(entity, request.version());
        folderValidator.validateUpdate(id, request);

        entity.setCode(request.code());
        entity.setName(request.name());
        entity.setOwner(resolveUser(request.ownerId()));
        if (request.active() != null) {
            entity.setActive(request.active());
        }
        entity.setParentFolder(request.parentFolderId() != null
                ? resolveParent(entity, request.parentFolderId())
                : null);

        return folderMapper.toDto(folderRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        Folder entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        folderRepository.save(entity);
    }

    private Folder resolveParent(Folder child, UUID parentFolderId) {
        Folder parent = findActiveById(parentFolderId);
        folderTreeValidator.validateParentAssignment(child, parent);
        return parent;
    }

    private User resolveUser(UUID userId) {
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Folder findActiveById(UUID id) {
        return folderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new FolderNotFoundException(id));
    }

    private Folder findActiveByCode(String code) {
        return folderRepository.findByCodeAndDeletedFalse(code)
                .orElseThrow(() -> new FolderNotFoundException(code));
    }

    private void assertVersion(Folder entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "Folder version mismatch for id: " + entity.getId());
        }
    }
}

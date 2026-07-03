package com.govos.doc.validator;

import com.govos.doc.entity.Folder;
import com.govos.doc.exception.InvalidHierarchyException;
import com.govos.doc.repository.FolderRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
public class FolderTreeValidator {

    private final FolderRepository folderRepository;

    public FolderTreeValidator(FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }

    public void validateParentAssignment(Folder child, Folder parent) {
        if (parent == null) {
            return;
        }
        if (child.getId() == null) {
            return;
        }
        if (child.getId().equals(parent.getId())) {
            throw new InvalidHierarchyException("Folder cannot be its own parent");
        }
        if (isAncestor(child.getId(), parent.getId())) {
            throw new InvalidHierarchyException(
                    "Parent assignment would create a cycle in the folder tree");
        }
    }

    private boolean isAncestor(UUID folderId, UUID ancestorId) {
        Set<UUID> visited = new HashSet<>();
        return walkUp(folderId, ancestorId, visited);
    }

    private boolean walkUp(UUID currentId, UUID targetAncestorId, Set<UUID> visited) {
        if (!visited.add(currentId)) {
            return false;
        }

        Folder current = folderRepository.findByIdAndDeletedFalse(currentId).orElse(null);
        if (current == null || current.getParentFolder() == null) {
            return false;
        }

        UUID parentId = current.getParentFolder().getId();
        if (parentId.equals(targetAncestorId)) {
            return true;
        }
        return walkUp(parentId, targetAncestorId, visited);
    }
}

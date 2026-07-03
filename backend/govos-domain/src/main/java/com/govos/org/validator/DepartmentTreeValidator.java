package com.govos.org.validator;

import com.govos.org.entity.Department;
import com.govos.org.entity.DepartmentHierarchy;
import com.govos.org.exception.InvalidHierarchyException;
import com.govos.org.repository.DepartmentHierarchyRepository;
import com.govos.org.repository.DepartmentRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Validates department tree integrity: same organization, no cycles, unlimited depth.
 */
@Component
public class DepartmentTreeValidator {

    private final DepartmentRepository departmentRepository;
    private final DepartmentHierarchyRepository departmentHierarchyRepository;

    public DepartmentTreeValidator(
            DepartmentRepository departmentRepository,
            DepartmentHierarchyRepository departmentHierarchyRepository) {
        this.departmentRepository = departmentRepository;
        this.departmentHierarchyRepository = departmentHierarchyRepository;
    }

    public void validateParentAssignment(Department child, Department parent) {
        if (parent == null) {
            return;
        }
        if (!child.getOrganization().getId().equals(parent.getOrganization().getId())) {
            throw new InvalidHierarchyException(
                    "Parent department must belong to the same organization");
        }
        if (child.getId() == null) {
            return;
        }
        if (child.getId().equals(parent.getId())) {
            throw new InvalidHierarchyException("Department cannot be its own parent");
        }
        if (isAncestor(child.getId(), parent.getId())) {
            throw new InvalidHierarchyException(
                    "Parent assignment would create a cycle in the department tree");
        }
    }

    public void validateHierarchyEdge(Department parent, Department child) {
        if (!parent.getOrganization().getId().equals(child.getOrganization().getId())) {
            throw new InvalidHierarchyException(
                    "Parent and child departments must belong to the same organization");
        }
        validateHierarchyEdge(parent.getId(), child.getId());
    }

    public void validateHierarchyEdge(UUID parentId, UUID childId) {
        if (parentId.equals(childId)) {
            throw new InvalidHierarchyException("Parent and child department cannot be the same");
        }
        if (isAncestor(childId, parentId)) {
            throw new InvalidHierarchyException(
                    "Hierarchy edge would create a cycle in the department tree");
        }
    }

    /**
     * Returns true if {@code ancestorId} is reachable by walking up from {@code departmentId}
     * via parentDepartment and explicit hierarchy child links.
     */
    private boolean isAncestor(UUID departmentId, UUID ancestorId) {
        Set<UUID> visited = new HashSet<>();
        return walkUp(departmentId, ancestorId, visited);
    }

    private boolean walkUp(UUID currentId, UUID targetAncestorId, Set<UUID> visited) {
        if (!visited.add(currentId)) {
            return false;
        }

        Department current = departmentRepository.findByIdAndDeletedFalse(currentId).orElse(null);
        if (current == null) {
            return false;
        }

        if (current.getParentDepartment() != null) {
            UUID parentId = current.getParentDepartment().getId();
            if (parentId.equals(targetAncestorId)) {
                return true;
            }
            if (walkUp(parentId, targetAncestorId, visited)) {
                return true;
            }
        }

        List<DepartmentHierarchy> parentEdges =
                departmentHierarchyRepository.findByChildDepartment_IdAndDeletedFalse(currentId);
        for (DepartmentHierarchy edge : parentEdges) {
            UUID parentId = edge.getParentDepartment().getId();
            if (parentId.equals(targetAncestorId)) {
                return true;
            }
            if (walkUp(parentId, targetAncestorId, visited)) {
                return true;
            }
        }

        return false;
    }
}

package com.govos.idm.repository;

import com.govos.idm.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {

    Optional<RolePermission> findByIdAndDeletedFalse(UUID id);

    List<RolePermission> findByRole_IdAndDeletedFalse(UUID roleId);

    List<RolePermission> findByPermission_IdAndDeletedFalse(UUID permissionId);

    boolean existsByRole_IdAndPermission_IdAndDeletedFalse(UUID roleId, UUID permissionId);
}

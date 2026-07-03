package com.govos.idm.repository;

import com.govos.idm.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByIdAndDeletedFalse(UUID id);

    Optional<Permission> findByCodeAndDeletedFalse(String code);

    List<Permission> findByModuleAndDeletedFalseOrderByResourceAsc(String module);

    boolean existsByCodeAndDeletedFalse(String code);

    boolean existsByModuleAndResourceAndActionAndDeletedFalse(String module, String resource, String action);
}

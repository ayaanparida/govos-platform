package com.govos.idm.repository;

import com.govos.idm.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    Optional<UserRole> findByIdAndDeletedFalse(UUID id);

    List<UserRole> findByUser_IdAndDeletedFalse(UUID userId);

    List<UserRole> findByRole_IdAndDeletedFalse(UUID roleId);

    boolean existsByUser_IdAndRole_IdAndDeletedFalse(UUID userId, UUID roleId);
}

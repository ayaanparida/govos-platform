package com.govos.idm.repository;

import com.govos.idm.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByIdAndDeletedFalse(UUID id);

    Optional<Role> findByCodeAndDeletedFalse(String code);

    List<Role> findByDeletedFalseOrderByNameAsc();

    boolean existsByCodeAndDeletedFalse(String code);
}

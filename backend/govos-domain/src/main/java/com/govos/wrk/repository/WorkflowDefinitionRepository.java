package com.govos.wrk.repository;

import com.govos.wrk.entity.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, UUID> {

    Optional<WorkflowDefinition> findByIdAndDeletedFalse(UUID id);

    Optional<WorkflowDefinition> findByCodeAndDeletedFalse(String code);

    List<WorkflowDefinition> findByDeletedFalseOrderByNameAsc();

    boolean existsByCodeAndDeletedFalse(String code);
}

package com.govos.cmp.repository;

import com.govos.cmp.entity.Complaint;
import com.govos.cmp.enums.ComplaintPriority;
import com.govos.cmp.enums.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {

    Optional<Complaint> findByIdAndDeletedFalse(UUID id);

    Optional<Complaint> findByCodeAndDeletedFalse(String code);

    boolean existsByCodeAndDeletedFalse(String code);

    List<Complaint> findAllByOrganizationIdAndDeletedFalse(UUID organizationId);

    List<Complaint> findAllByCitizenUserIdAndDeletedFalse(UUID citizenUserId);

    List<Complaint> findAllByAssignedOfficerIdAndDeletedFalse(UUID assignedOfficerId);

    List<Complaint> findAllByStatusAndDeletedFalse(ComplaintStatus status);

    List<Complaint> findAllByPriorityAndDeletedFalse(ComplaintPriority priority);

    List<Complaint> findAllByCategoryKeyAndDeletedFalse(String categoryKey);

    List<Complaint> findAllByWorkflowInstanceIdAndDeletedFalse(UUID workflowInstanceId);

    List<Complaint> findAllByOrganizationIdAndStatusAndDeletedFalse(UUID organizationId, ComplaintStatus status);

    Page<Complaint> findAllByDeletedFalse(Pageable pageable);
}

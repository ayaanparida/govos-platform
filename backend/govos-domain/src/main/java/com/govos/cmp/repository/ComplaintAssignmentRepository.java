package com.govos.cmp.repository;

import com.govos.cmp.entity.ComplaintAssignment;
import com.govos.cmp.enums.ComplaintAssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComplaintAssignmentRepository extends JpaRepository<ComplaintAssignment, UUID> {

    List<ComplaintAssignment> findAllByComplaintIdAndDeletedFalse(UUID complaintId);

    Optional<ComplaintAssignment> findByComplaintIdAndIsCurrentTrueAndDeletedFalse(UUID complaintId);

    List<ComplaintAssignment> findAllByOfficerUserIdAndDeletedFalse(UUID officerUserId);

    List<ComplaintAssignment> findAllByAssignmentStatusAndDeletedFalse(ComplaintAssignmentStatus assignmentStatus);
}

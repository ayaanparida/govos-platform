package com.govos.cmp.repository;

import com.govos.cmp.entity.ComplaintEscalation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComplaintEscalationRepository extends JpaRepository<ComplaintEscalation, UUID> {

    List<ComplaintEscalation> findAllByComplaintIdAndDeletedFalseOrderByEscalatedAtAsc(UUID complaintId);
}

package com.govos.cmp.repository;

import com.govos.cmp.entity.ComplaintFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComplaintFeedbackRepository extends JpaRepository<ComplaintFeedback, UUID> {

    Optional<ComplaintFeedback> findByComplaintIdAndDeletedFalse(UUID complaintId);

    boolean existsByComplaintIdAndDeletedFalse(UUID complaintId);
}

package com.govos.cmp.repository;

import com.govos.cmp.entity.ComplaintDuplicate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComplaintDuplicateRepository extends JpaRepository<ComplaintDuplicate, UUID> {

    @Query("SELECT d FROM ComplaintDuplicate d WHERE d.complaint.id = :primaryComplaintId AND d.deleted = false")
    List<ComplaintDuplicate> findAllByPrimaryComplaintIdAndDeletedFalse(@Param("primaryComplaintId") UUID primaryComplaintId);

    List<ComplaintDuplicate> findAllByDuplicateComplaintIdAndDeletedFalse(UUID duplicateComplaintId);
}

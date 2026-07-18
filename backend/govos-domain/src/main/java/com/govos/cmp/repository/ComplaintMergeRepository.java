package com.govos.cmp.repository;

import com.govos.cmp.entity.ComplaintMerge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComplaintMergeRepository extends JpaRepository<ComplaintMerge, UUID> {

    @Query("SELECT m FROM ComplaintMerge m WHERE m.complaint.id = :survivingComplaintId AND m.deleted = false")
    List<ComplaintMerge> findAllBySurvivingComplaintIdAndDeletedFalse(@Param("survivingComplaintId") UUID survivingComplaintId);

    List<ComplaintMerge> findAllByMergedComplaintIdAndDeletedFalse(UUID mergedComplaintId);
}

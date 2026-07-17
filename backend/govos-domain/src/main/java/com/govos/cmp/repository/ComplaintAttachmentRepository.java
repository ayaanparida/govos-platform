package com.govos.cmp.repository;

import com.govos.cmp.entity.ComplaintAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComplaintAttachmentRepository extends JpaRepository<ComplaintAttachment, UUID> {

    List<ComplaintAttachment> findAllByComplaintIdAndDeletedFalse(UUID complaintId);

    List<ComplaintAttachment> findAllByDocumentIdAndDeletedFalse(UUID documentId);
}

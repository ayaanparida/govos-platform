package com.govos.cmp.repository;

import com.govos.cmp.entity.ComplaintComment;
import com.govos.cmp.enums.ComplaintVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComplaintCommentRepository extends JpaRepository<ComplaintComment, UUID> {

    List<ComplaintComment> findAllByComplaintIdAndDeletedFalseOrderByCreatedDateAsc(UUID complaintId);

    List<ComplaintComment> findAllByComplaintIdAndVisibilityAndDeletedFalse(UUID complaintId, ComplaintVisibility visibility);
}

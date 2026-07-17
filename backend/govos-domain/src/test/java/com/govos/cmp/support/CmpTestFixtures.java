package com.govos.cmp.support;

import com.govos.cmp.dto.ComplaintAssignmentCreateRequest;
import com.govos.cmp.dto.ComplaintAttachmentCreateRequest;
import com.govos.cmp.dto.ComplaintCommentCreateRequest;
import com.govos.cmp.dto.ComplaintCreateRequest;
import com.govos.cmp.dto.ComplaintDuplicateCreateRequest;
import com.govos.cmp.dto.ComplaintEscalationCreateRequest;
import com.govos.cmp.dto.ComplaintFeedbackCreateRequest;
import com.govos.cmp.dto.ComplaintFeedbackUpdateRequest;
import com.govos.cmp.dto.ComplaintMergeCreateRequest;
import com.govos.cmp.dto.ComplaintUpdateRequest;
import com.govos.cmp.entity.Complaint;
import com.govos.cmp.entity.ComplaintAssignment;
import com.govos.cmp.entity.ComplaintAttachment;
import com.govos.cmp.entity.ComplaintComment;
import com.govos.cmp.entity.ComplaintDuplicate;
import com.govos.cmp.entity.ComplaintEscalation;
import com.govos.cmp.entity.ComplaintFeedback;
import com.govos.cmp.entity.ComplaintMerge;
import com.govos.cmp.entity.ComplaintStatusHistory;
import com.govos.cmp.enums.ComplaintAssignmentType;
import com.govos.cmp.enums.ComplaintAttachmentType;
import com.govos.cmp.enums.ComplaintCommentType;
import com.govos.cmp.enums.ComplaintDuplicateDetectedBy;
import com.govos.cmp.enums.ComplaintEscalationLevel;
import com.govos.cmp.enums.ComplaintEscalationReason;
import com.govos.cmp.enums.ComplaintFeedbackRating;
import com.govos.cmp.enums.ComplaintPriority;
import com.govos.cmp.enums.ComplaintSource;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.cmp.enums.ComplaintVisibility;
import com.govos.cmp.valueobject.ComplaintLocation;
import com.govos.mdm.entity.MasterData;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class CmpTestFixtures {

    public static final UUID ORG_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID CITIZEN_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID OFFICER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final UUID DEPARTMENT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    public static final UUID OFFICE_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    public static final UUID DOCUMENT_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    public static final String CATEGORY_KEY = "WATER_SUPPLY";
    public static final String SUB_CATEGORY_KEY = "PIPE_LEAK";

    private CmpTestFixtures() {
    }

    public static Complaint complaint(UUID id, ComplaintStatus status) {
        Complaint complaint = new Complaint();
        complaint.setId(id);
        complaint.setCode("CMP-2026-0001");
        complaint.setTitle("Water leak on Main Street");
        complaint.setDescription("Pipe burst near junction");
        complaint.setStatus(status);
        complaint.setPriority(ComplaintPriority.MEDIUM);
        complaint.setSource(ComplaintSource.CITIZEN_PORTAL);
        complaint.setCategoryKey(CATEGORY_KEY);
        complaint.setSubCategoryKey(SUB_CATEGORY_KEY);
        complaint.setCitizenUserId(CITIZEN_ID);
        complaint.setOrganizationId(ORG_ID);
        complaint.setLocation(location());
        complaint.setActive(true);
        complaint.setDeleted(false);
        complaint.setVersion(0L);
        complaint.setIsDuplicate(false);
        return complaint;
    }

    public static ComplaintLocation location() {
        ComplaintLocation location = new ComplaintLocation();
        location.setStateKey("KA");
        location.setDistrictKey("BLR");
        location.setLatitude(new BigDecimal("12.9716000"));
        location.setLongitude(new BigDecimal("77.5946000"));
        location.setAddress("Main Street");
        return location;
    }

    public static ComplaintCreateRequest createRequest() {
        return new ComplaintCreateRequest(
                "Water leak on Main Street",
                "Pipe burst near junction",
                ComplaintPriority.MEDIUM,
                ComplaintSource.CITIZEN_PORTAL,
                "WEB",
                CATEGORY_KEY,
                SUB_CATEGORY_KEY,
                "SERVICE_REQUEST",
                CITIZEN_ID,
                CITIZEN_ID,
                ORG_ID,
                DEPARTMENT_ID,
                OFFICE_ID,
                "KA",
                "BLR",
                null,
                null,
                null,
                new BigDecimal("12.9716000"),
                new BigDecimal("77.5946000"),
                "Main Street",
                null,
                "560001",
                null,
                true);
    }

    public static ComplaintUpdateRequest updateRequest() {
        return new ComplaintUpdateRequest(
                "Updated title",
                "Updated description",
                ComplaintPriority.HIGH,
                "WEB",
                CATEGORY_KEY,
                SUB_CATEGORY_KEY,
                "SERVICE_REQUEST",
                DEPARTMENT_ID,
                OFFICE_ID,
                "KA",
                "BLR",
                null,
                null,
                null,
                new BigDecimal("12.9716000"),
                new BigDecimal("77.5946000"),
                "Main Street",
                null,
                "560001",
                null,
                true,
                0L);
    }

    public static ComplaintAssignmentCreateRequest assignmentCreateRequest(UUID complaintId) {
        return new ComplaintAssignmentCreateRequest(
                complaintId,
                ComplaintAssignmentType.INITIAL,
                DEPARTMENT_ID,
                OFFICE_ID,
                OFFICER_ID,
                OFFICER_ID,
                "Assign to field officer",
                true);
    }

    public static ComplaintCommentCreateRequest commentCreateRequest(UUID complaintId) {
        return new ComplaintCommentCreateRequest(
                complaintId,
                OFFICER_ID,
                "Investigating the issue",
                ComplaintVisibility.INTERNAL,
                ComplaintCommentType.REMARK,
                true);
    }

    public static ComplaintAttachmentCreateRequest attachmentCreateRequest(UUID complaintId) {
        return new ComplaintAttachmentCreateRequest(
                complaintId,
                DOCUMENT_ID,
                null,
                ComplaintAttachmentType.DOCUMENT,
                "Site photo",
                OFFICER_ID,
                1,
                true);
    }

    public static ComplaintFeedbackCreateRequest feedbackCreateRequest(UUID complaintId) {
        return new ComplaintFeedbackCreateRequest(
                complaintId,
                CITIZEN_ID,
                ComplaintFeedbackRating.FOUR,
                "Issue resolved quickly",
                Instant.parse("2026-01-15T10:00:00Z"),
                true);
    }

    public static ComplaintFeedbackUpdateRequest feedbackUpdateRequest() {
        return new ComplaintFeedbackUpdateRequest(
                ComplaintFeedbackRating.FIVE,
                "Excellent service",
                true,
                0L);
    }

    public static ComplaintEscalationCreateRequest escalationCreateRequest(UUID complaintId) {
        return new ComplaintEscalationCreateRequest(
                complaintId,
                ComplaintEscalationLevel.L2,
                ComplaintEscalationReason.SLA_BREACH,
                OFFICER_ID,
                null,
                DEPARTMENT_ID,
                "SLA breached",
                Instant.parse("2026-01-10T08:00:00Z"),
                true);
    }

    public static ComplaintDuplicateCreateRequest duplicateCreateRequest(UUID primaryId, UUID duplicateId) {
        return new ComplaintDuplicateCreateRequest(
                primaryId,
                duplicateId,
                ComplaintDuplicateDetectedBy.MANUAL,
                OFFICER_ID,
                new BigDecimal("0.9500"),
                "Same location and category",
                true);
    }

    public static ComplaintMergeCreateRequest mergeCreateRequest(UUID survivingId, UUID mergedId) {
        return new ComplaintMergeCreateRequest(
                survivingId,
                mergedId,
                OFFICER_ID,
                "Same grievance",
                Instant.parse("2026-01-12T09:00:00Z"),
                true);
    }

    public static ComplaintAssignment assignment(UUID id, UUID complaintId) {
        ComplaintAssignment assignment = new ComplaintAssignment();
        assignment.setId(id);
        assignment.setComplaint(complaint(complaintId, ComplaintStatus.ASSIGNED));
        assignment.setAssignmentType(ComplaintAssignmentType.INITIAL);
        assignment.setOfficerUserId(OFFICER_ID);
        assignment.setDepartmentId(DEPARTMENT_ID);
        assignment.setOfficeId(OFFICE_ID);
        assignment.setIsCurrent(true);
        assignment.setActive(true);
        assignment.setDeleted(false);
        return assignment;
    }

    public static ComplaintComment comment(UUID id, UUID complaintId) {
        ComplaintComment comment = new ComplaintComment();
        comment.setId(id);
        comment.setComplaint(complaint(complaintId, ComplaintStatus.IN_PROGRESS));
        comment.setAuthorUserId(OFFICER_ID);
        comment.setCommentText("Investigating");
        comment.setVisibility(ComplaintVisibility.INTERNAL);
        comment.setCommentType(ComplaintCommentType.REMARK);
        comment.setActive(true);
        comment.setDeleted(false);
        return comment;
    }

    public static ComplaintAttachment attachment(UUID id, UUID complaintId) {
        ComplaintAttachment attachment = new ComplaintAttachment();
        attachment.setId(id);
        attachment.setComplaint(complaint(complaintId, ComplaintStatus.IN_PROGRESS));
        attachment.setDocumentId(DOCUMENT_ID);
        attachment.setAttachmentType(ComplaintAttachmentType.DOCUMENT);
        attachment.setActive(true);
        attachment.setDeleted(false);
        return attachment;
    }

    public static ComplaintFeedback feedback(UUID id, UUID complaintId) {
        ComplaintFeedback feedback = new ComplaintFeedback();
        feedback.setId(id);
        feedback.setComplaint(complaint(complaintId, ComplaintStatus.CLOSED));
        feedback.setRatedByUserId(CITIZEN_ID);
        feedback.setRating(ComplaintFeedbackRating.FOUR);
        feedback.setRatedAt(Instant.parse("2026-01-15T10:00:00Z"));
        feedback.setActive(true);
        feedback.setDeleted(false);
        feedback.setVersion(0L);
        return feedback;
    }

    public static ComplaintEscalation escalation(UUID id, UUID complaintId) {
        ComplaintEscalation escalation = new ComplaintEscalation();
        escalation.setId(id);
        escalation.setComplaint(complaint(complaintId, ComplaintStatus.IN_PROGRESS));
        escalation.setEscalationLevel(ComplaintEscalationLevel.L2);
        escalation.setEscalationReason(ComplaintEscalationReason.SLA_BREACH);
        escalation.setEscalatedAt(Instant.parse("2026-01-10T08:00:00Z"));
        escalation.setActive(true);
        escalation.setDeleted(false);
        return escalation;
    }

    public static ComplaintDuplicate duplicateLink(UUID id, UUID primaryId, UUID duplicateId) {
        ComplaintDuplicate duplicate = new ComplaintDuplicate();
        duplicate.setId(id);
        duplicate.setComplaint(complaint(primaryId, ComplaintStatus.IN_PROGRESS));
        duplicate.setDuplicateComplaintId(duplicateId);
        duplicate.setDetectedBy(ComplaintDuplicateDetectedBy.MANUAL);
        duplicate.setActive(true);
        duplicate.setDeleted(false);
        return duplicate;
    }

    public static ComplaintMerge mergeLink(UUID id, UUID survivingId, UUID mergedId) {
        ComplaintMerge merge = new ComplaintMerge();
        merge.setId(id);
        merge.setComplaint(complaint(survivingId, ComplaintStatus.IN_PROGRESS));
        merge.setMergedComplaintId(mergedId);
        merge.setMergedByUserId(OFFICER_ID);
        merge.setMergedAt(Instant.parse("2026-01-12T09:00:00Z"));
        merge.setActive(true);
        merge.setDeleted(false);
        return merge;
    }

    public static ComplaintStatusHistory statusHistory(UUID complaintId) {
        ComplaintStatusHistory history = new ComplaintStatusHistory();
        history.setComplaint(complaint(complaintId, ComplaintStatus.SUBMITTED));
        history.setFromStatus(ComplaintStatus.DRAFT);
        history.setToStatus(ComplaintStatus.SUBMITTED);
        history.setChangedByUserId(CITIZEN_ID);
        history.setOccurredAt(Instant.parse("2026-01-01T10:00:00Z"));
        history.setActive(true);
        history.setDeleted(false);
        return history;
    }

    public static MasterData masterData(String type, String key) {
        MasterData masterData = new MasterData();
        masterData.setId(UUID.randomUUID());
        masterData.setType(type);
        masterData.setKey(key);
        masterData.setValue(key);
        masterData.setActive(true);
        masterData.setDeleted(false);
        return masterData;
    }
}

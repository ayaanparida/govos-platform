package com.govos.cmp.entity;

import com.govos.cmp.enums.ComplaintCommentType;
import com.govos.cmp.enums.ComplaintVisibility;
import com.govos.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Entity
@Table(name = "cmp_complaint_comment", schema = "govos")
public class ComplaintComment extends AuditableEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @NotNull
    @Column(name = "author_user_id", nullable = false)
    private UUID authorUserId;

    @NotBlank
    @Column(name = "comment_text", nullable = false, columnDefinition = "TEXT")
    private String commentText;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 20)
    private ComplaintVisibility visibility = ComplaintVisibility.INTERNAL;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "comment_type", nullable = false, length = 30)
    private ComplaintCommentType commentType = ComplaintCommentType.REMARK;

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public UUID getAuthorUserId() {
        return authorUserId;
    }

    public void setAuthorUserId(UUID authorUserId) {
        this.authorUserId = authorUserId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public ComplaintVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(ComplaintVisibility visibility) {
        this.visibility = visibility;
    }

    public ComplaintCommentType getCommentType() {
        return commentType;
    }

    public void setCommentType(ComplaintCommentType commentType) {
        this.commentType = commentType;
    }
}

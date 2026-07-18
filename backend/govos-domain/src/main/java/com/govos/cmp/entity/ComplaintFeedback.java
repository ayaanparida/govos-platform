package com.govos.cmp.entity;

import com.govos.cmp.enums.ComplaintFeedbackRating;
import com.govos.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cmp_complaint_feedback", schema = "govos")
public class ComplaintFeedback extends AuditableEntity {

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "complaint_id", nullable = false, unique = true)
    private Complaint complaint;

    @NotNull
    @Column(name = "rated_by_user_id", nullable = false)
    private UUID ratedByUserId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "rating", nullable = false, length = 10)
    private ComplaintFeedbackRating rating;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @NotNull
    @PastOrPresent
    @Column(name = "rated_at", nullable = false)
    private Instant ratedAt;

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public UUID getRatedByUserId() {
        return ratedByUserId;
    }

    public void setRatedByUserId(UUID ratedByUserId) {
        this.ratedByUserId = ratedByUserId;
    }

    public ComplaintFeedbackRating getRating() {
        return rating;
    }

    public void setRating(ComplaintFeedbackRating rating) {
        this.rating = rating;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Instant getRatedAt() {
        return ratedAt;
    }

    public void setRatedAt(Instant ratedAt) {
        this.ratedAt = ratedAt;
    }
}

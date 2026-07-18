-- =============================================================================
-- GovOS Flyway Migration V2.0.0 — Complaint Management (CMP)
-- =============================================================================
-- Version     : 2.0.0
-- Purpose     : Citizen Grievance Management System (CGMS) domain schema
-- Author      : GovOS Platform Team
-- Architecture: CMP-001.6 (frozen), CMP-002.1 entities, CMP-003 migration
-- Dependencies: V1__baseline.sql (schema govos, pgcrypto)
--               V1_1_0 through V1_7_0 (platform foundation)
-- ADR         : ADR-002 Modular Monolith, ADR-008 Flyway
-- Notes       : ComplaintLocation embedded in cmp_complaint (no cmp_complaint_location)
--               Cross-module references are UUID columns only (no FK to IDM/ORG/DOC/WRK)
-- =============================================================================

-- ---------------------------------------------------------------------------
-- cmp_complaint (aggregate root)
-- ---------------------------------------------------------------------------
CREATE TABLE govos.cmp_complaint (
    id                          UUID            NOT NULL DEFAULT gen_random_uuid(),
    code                        VARCHAR(100),
    active                      BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                     BOOLEAN         NOT NULL DEFAULT FALSE,
    version                     BIGINT          NOT NULL DEFAULT 0,
    created_by                  VARCHAR(100)    NOT NULL,
    created_date                TIMESTAMPTZ     NOT NULL,
    updated_by                  VARCHAR(100)    NOT NULL,
    updated_date                TIMESTAMPTZ     NOT NULL,
    title                       VARCHAR(500)    NOT NULL,
    description                 TEXT,
    status                      VARCHAR(30)     NOT NULL DEFAULT 'DRAFT',
    priority                    VARCHAR(20)     NOT NULL DEFAULT 'MEDIUM',
    source                      VARCHAR(30)     NOT NULL,
    channel                     VARCHAR(100),
    category_key                VARCHAR(100),
    sub_category_key            VARCHAR(100),
    complaint_type_key          VARCHAR(100),
    citizen_user_id             UUID,
    submitted_by_user_id        UUID,
    organization_id             UUID,
    department_id               UUID,
    office_id                   UUID,
    assigned_officer_id         UUID,
    resolved_by_user_id         UUID,
    closed_by_user_id           UUID,
    workflow_instance_id        UUID,
    submitted_at                TIMESTAMPTZ,
    closed_at                   TIMESTAMPTZ,
    archived_at                 TIMESTAMPTZ,
    rejection_reason_key        VARCHAR(100),
    closure_reason_key          VARCHAR(100),
    is_duplicate                BOOLEAN         NOT NULL DEFAULT FALSE,
    primary_complaint_id        UUID,
    merged_into_complaint_id    UUID,
    -- Embedded ComplaintLocation
    state_key                   VARCHAR(100),
    district_key                VARCHAR(100),
    ulb_key                     VARCHAR(100),
    ward_key                    VARCHAR(100),
    village_key                 VARCHAR(100),
    latitude                    NUMERIC(10, 7),
    longitude                   NUMERIC(10, 7),
    address                     VARCHAR(1000),
    landmark                    VARCHAR(255),
    pincode                     VARCHAR(20),
    geo_json                    TEXT,
    CONSTRAINT pk_cmp_complaint PRIMARY KEY (id),
    CONSTRAINT chk_cmp_complaint_latitude
        CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90)),
    CONSTRAINT chk_cmp_complaint_longitude
        CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180))
);

COMMENT ON TABLE govos.cmp_complaint IS
    'Aggregate root for citizen grievance records (CGMS). ComplaintLocation is embedded.';
COMMENT ON COLUMN govos.cmp_complaint.code IS
    'Business complaint number, e.g. CMP-2026-000042';
COMMENT ON COLUMN govos.cmp_complaint.status IS
    'Business lifecycle status; assignment decline uses PENDING_REASSIGNMENT, not REJECTED';
COMMENT ON COLUMN govos.cmp_complaint.rejection_reason_key IS
    'MDM key COMPLAINT_REJECTION_REASON — intake rejection only';
COMMENT ON COLUMN govos.cmp_complaint.workflow_instance_id IS
    'Denormalized reference to WRK workflow instance; WRK owns process state';
COMMENT ON COLUMN govos.cmp_complaint.citizen_user_id IS
    'Reference to idm_user (UUID only — no FK)';
COMMENT ON COLUMN govos.cmp_complaint.organization_id IS
    'Jurisdiction / tenant anchor — reference to org_organization (UUID only — no FK)';
COMMENT ON COLUMN govos.cmp_complaint.primary_complaint_id IS
    'Self-reference when marked duplicate (UUID only — no FK)';
COMMENT ON COLUMN govos.cmp_complaint.ulb_key IS
    'MDM key COMPLAINT_ULB — Urban Local Body for municipal routing';
COMMENT ON COLUMN govos.cmp_complaint.geo_json IS
    'Reserved for future GIS polygon or pin geometry (GeoJSON)';

CREATE UNIQUE INDEX uk_cmp_complaint_code_active
    ON govos.cmp_complaint (code)
    WHERE deleted = FALSE AND code IS NOT NULL;

CREATE INDEX idx_cmp_complaint_organization_id
    ON govos.cmp_complaint (organization_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_cmp_complaint_workflow_instance_id
    ON govos.cmp_complaint (workflow_instance_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_cmp_complaint_status
    ON govos.cmp_complaint (status)
    WHERE deleted = FALSE;

CREATE INDEX idx_cmp_complaint_priority
    ON govos.cmp_complaint (priority)
    WHERE deleted = FALSE;

CREATE INDEX idx_cmp_complaint_category_key
    ON govos.cmp_complaint (category_key)
    WHERE deleted = FALSE;

CREATE INDEX idx_cmp_complaint_sub_category_key
    ON govos.cmp_complaint (sub_category_key)
    WHERE deleted = FALSE;

CREATE INDEX idx_cmp_complaint_citizen_user_id
    ON govos.cmp_complaint (citizen_user_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_cmp_complaint_assigned_officer_id
    ON govos.cmp_complaint (assigned_officer_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_cmp_complaint_created_date
    ON govos.cmp_complaint (created_date)
    WHERE deleted = FALSE;

CREATE INDEX idx_cmp_complaint_updated_date
    ON govos.cmp_complaint (updated_date)
    WHERE deleted = FALSE;

CREATE INDEX idx_cmp_complaint_deleted
    ON govos.cmp_complaint (deleted);

CREATE INDEX idx_cmp_complaint_latitude
    ON govos.cmp_complaint (latitude)
    WHERE deleted = FALSE AND latitude IS NOT NULL;

CREATE INDEX idx_cmp_complaint_longitude
    ON govos.cmp_complaint (longitude)
    WHERE deleted = FALSE AND longitude IS NOT NULL;

CREATE INDEX idx_cmp_complaint_primary_complaint_id
    ON govos.cmp_complaint (primary_complaint_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_cmp_complaint_merged_into_complaint_id
    ON govos.cmp_complaint (merged_into_complaint_id)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- cmp_complaint_assignment
-- ---------------------------------------------------------------------------
CREATE TABLE govos.cmp_complaint_assignment (
    id                      UUID            NOT NULL DEFAULT gen_random_uuid(),
    code                    VARCHAR(100),
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    complaint_id            UUID            NOT NULL,
    assignment_type         VARCHAR(20)     NOT NULL,
    department_id           UUID,
    office_id               UUID,
    officer_user_id         UUID,
    assigned_by_user_id     UUID,
    assignment_status       VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    assigned_at             TIMESTAMPTZ,
    accepted_at             TIMESTAMPTZ,
    rejected_at             TIMESTAMPTZ,
    rejection_reason_key    VARCHAR(100),
    remarks                 TEXT,
    is_current              BOOLEAN         NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_cmp_complaint_assignment PRIMARY KEY (id),
    CONSTRAINT fk_cmp_complaint_assignment_complaint
        FOREIGN KEY (complaint_id) REFERENCES govos.cmp_complaint (id)
);

COMMENT ON TABLE govos.cmp_complaint_assignment IS
    'Append-only assignment history; officer decline sets assignment_status REJECTED and complaint PENDING_REASSIGNMENT';
COMMENT ON COLUMN govos.cmp_complaint_assignment.is_current IS
    'Exactly one current assignment per complaint at a time';
COMMENT ON COLUMN govos.cmp_complaint_assignment.department_id IS
    'Reference to org_department (UUID only — no FK)';

CREATE INDEX idx_cmp_complaint_assignment_complaint_id
    ON govos.cmp_complaint_assignment (complaint_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_cmp_complaint_assignment_officer_user_id
    ON govos.cmp_complaint_assignment (officer_user_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_cmp_complaint_assignment_is_current
    ON govos.cmp_complaint_assignment (complaint_id, is_current)
    WHERE deleted = FALSE AND is_current = TRUE;

-- ---------------------------------------------------------------------------
-- cmp_complaint_comment
-- ---------------------------------------------------------------------------
CREATE TABLE govos.cmp_complaint_comment (
    id                  UUID            NOT NULL DEFAULT gen_random_uuid(),
    code                VARCHAR(100),
    active              BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             BIGINT          NOT NULL DEFAULT 0,
    created_by          VARCHAR(100)    NOT NULL,
    created_date        TIMESTAMPTZ     NOT NULL,
    updated_by          VARCHAR(100)    NOT NULL,
    updated_date        TIMESTAMPTZ     NOT NULL,
    complaint_id        UUID            NOT NULL,
    author_user_id      UUID            NOT NULL,
    comment_text        TEXT            NOT NULL,
    visibility          VARCHAR(20)     NOT NULL DEFAULT 'INTERNAL',
    comment_type        VARCHAR(30)     NOT NULL DEFAULT 'REMARK',
    CONSTRAINT pk_cmp_complaint_comment PRIMARY KEY (id),
    CONSTRAINT fk_cmp_complaint_comment_complaint
        FOREIGN KEY (complaint_id) REFERENCES govos.cmp_complaint (id)
);

COMMENT ON TABLE govos.cmp_complaint_comment IS
    'Internal and citizen-visible remarks on a complaint';
COMMENT ON COLUMN govos.cmp_complaint_comment.visibility IS
    'INTERNAL or CITIZEN_VISIBLE';

CREATE INDEX idx_cmp_complaint_comment_complaint_id
    ON govos.cmp_complaint_comment (complaint_id)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- cmp_complaint_attachment
-- ---------------------------------------------------------------------------
CREATE TABLE govos.cmp_complaint_attachment (
    id                      UUID            NOT NULL DEFAULT gen_random_uuid(),
    code                    VARCHAR(100),
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    complaint_id            UUID            NOT NULL,
    document_id             UUID            NOT NULL,
    document_version_id     UUID,
    attachment_type         VARCHAR(20)     NOT NULL,
    display_name            VARCHAR(255),
    uploaded_by_user_id     UUID            NOT NULL,
    sort_order              INTEGER,
    CONSTRAINT pk_cmp_complaint_attachment PRIMARY KEY (id),
    CONSTRAINT fk_cmp_complaint_attachment_complaint
        FOREIGN KEY (complaint_id) REFERENCES govos.cmp_complaint (id)
);

COMMENT ON TABLE govos.cmp_complaint_attachment IS
    'Metadata link to DOC module; no binary content stored in CMP';
COMMENT ON COLUMN govos.cmp_complaint_attachment.document_id IS
    'Reference to doc_document (UUID only — no FK)';
COMMENT ON COLUMN govos.cmp_complaint_attachment.document_version_id IS
    'Optional pinned doc_document_version for evidence integrity';

CREATE INDEX idx_cmp_complaint_attachment_complaint_id
    ON govos.cmp_complaint_attachment (complaint_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_cmp_complaint_attachment_document_id
    ON govos.cmp_complaint_attachment (document_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_cmp_complaint_attachment_document_version_id
    ON govos.cmp_complaint_attachment (document_version_id)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- cmp_complaint_status_history (append-only business status log)
-- ---------------------------------------------------------------------------
CREATE TABLE govos.cmp_complaint_status_history (
    id                  UUID            NOT NULL DEFAULT gen_random_uuid(),
    code                VARCHAR(100),
    active              BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             BIGINT          NOT NULL DEFAULT 0,
    created_by          VARCHAR(100)    NOT NULL,
    created_date        TIMESTAMPTZ     NOT NULL,
    updated_by          VARCHAR(100)    NOT NULL,
    updated_date        TIMESTAMPTZ     NOT NULL,
    complaint_id        UUID            NOT NULL,
    from_status         VARCHAR(30),
    to_status           VARCHAR(30)     NOT NULL,
    changed_by_user_id  UUID            NOT NULL,
    reason              TEXT,
    reason_key          VARCHAR(100),
    occurred_at         TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_cmp_complaint_status_history PRIMARY KEY (id),
    CONSTRAINT fk_cmp_complaint_status_history_complaint
        FOREIGN KEY (complaint_id) REFERENCES govos.cmp_complaint (id)
);

COMMENT ON TABLE govos.cmp_complaint_status_history IS
    'Append-only complaint state machine history (CMP-specific; AUD holds compliance audit)';

CREATE INDEX idx_cmp_complaint_status_history_complaint_id
    ON govos.cmp_complaint_status_history (complaint_id);

CREATE INDEX idx_cmp_complaint_status_history_occurred_at
    ON govos.cmp_complaint_status_history (occurred_at);

-- ---------------------------------------------------------------------------
-- cmp_complaint_feedback (1:1 with complaint)
-- ---------------------------------------------------------------------------
CREATE TABLE govos.cmp_complaint_feedback (
    id                  UUID            NOT NULL DEFAULT gen_random_uuid(),
    code                VARCHAR(100),
    active              BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             BIGINT          NOT NULL DEFAULT 0,
    created_by          VARCHAR(100)    NOT NULL,
    created_date        TIMESTAMPTZ     NOT NULL,
    updated_by          VARCHAR(100)    NOT NULL,
    updated_date        TIMESTAMPTZ     NOT NULL,
    complaint_id        UUID            NOT NULL,
    rated_by_user_id    UUID            NOT NULL,
    rating              VARCHAR(10)     NOT NULL,
    feedback            TEXT,
    rated_at            TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_cmp_complaint_feedback PRIMARY KEY (id),
    CONSTRAINT fk_cmp_complaint_feedback_complaint
        FOREIGN KEY (complaint_id) REFERENCES govos.cmp_complaint (id),
    CONSTRAINT chk_cmp_complaint_feedback_rating
        CHECK (rating IN ('ONE', 'TWO', 'THREE', 'FOUR', 'FIVE'))
);

COMMENT ON TABLE govos.cmp_complaint_feedback IS
    'Citizen satisfaction feedback after complaint closure';
COMMENT ON COLUMN govos.cmp_complaint_feedback.rating IS
    'ComplaintFeedbackRating enum stored as VARCHAR (ONE through FIVE)';

CREATE UNIQUE INDEX uk_cmp_complaint_feedback_complaint_active
    ON govos.cmp_complaint_feedback (complaint_id)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- cmp_complaint_escalation (append-only)
-- ---------------------------------------------------------------------------
CREATE TABLE govos.cmp_complaint_escalation (
    id                          UUID            NOT NULL DEFAULT gen_random_uuid(),
    code                        VARCHAR(100),
    active                      BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                     BOOLEAN         NOT NULL DEFAULT FALSE,
    version                     BIGINT          NOT NULL DEFAULT 0,
    created_by                  VARCHAR(100)    NOT NULL,
    created_date                TIMESTAMPTZ     NOT NULL,
    updated_by                  VARCHAR(100)    NOT NULL,
    updated_date                TIMESTAMPTZ     NOT NULL,
    complaint_id                UUID            NOT NULL,
    escalation_level            VARCHAR(30)     NOT NULL,
    escalation_reason           VARCHAR(30)     NOT NULL,
    escalated_by_user_id        UUID            NOT NULL,
    escalated_to_user_id        UUID,
    escalated_to_department_id  UUID,
    remarks                     TEXT,
    escalated_at                TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_cmp_complaint_escalation PRIMARY KEY (id),
    CONSTRAINT fk_cmp_complaint_escalation_complaint
        FOREIGN KEY (complaint_id) REFERENCES govos.cmp_complaint (id)
);

COMMENT ON TABLE govos.cmp_complaint_escalation IS
    'Append-only escalation events for SLA breach or manual escalation';

CREATE INDEX idx_cmp_complaint_escalation_complaint_id
    ON govos.cmp_complaint_escalation (complaint_id);

CREATE INDEX idx_cmp_complaint_escalation_escalated_at
    ON govos.cmp_complaint_escalation (escalated_at);

-- ---------------------------------------------------------------------------
-- cmp_complaint_duplicate
-- ---------------------------------------------------------------------------
CREATE TABLE govos.cmp_complaint_duplicate (
    id                      UUID            NOT NULL DEFAULT gen_random_uuid(),
    code                    VARCHAR(100),
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    primary_complaint_id    UUID            NOT NULL,
    duplicate_complaint_id  UUID            NOT NULL,
    detected_by             VARCHAR(20)     NOT NULL,
    detected_by_user_id     UUID,
    similarity_score        NUMERIC(5, 4),
    remarks                 TEXT,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT pk_cmp_complaint_duplicate PRIMARY KEY (id),
    CONSTRAINT fk_cmp_complaint_duplicate_primary
        FOREIGN KEY (primary_complaint_id) REFERENCES govos.cmp_complaint (id)
);

COMMENT ON TABLE govos.cmp_complaint_duplicate IS
    'Duplicate link from primary complaint; duplicate_complaint_id is UUID only (cross-aggregate, no FK)';
COMMENT ON COLUMN govos.cmp_complaint_duplicate.duplicate_complaint_id IS
    'Reference to duplicate cmp_complaint (UUID only — no FK per cross-aggregate orchestration)';

CREATE UNIQUE INDEX uk_cmp_complaint_duplicate_pair_active
    ON govos.cmp_complaint_duplicate (primary_complaint_id, duplicate_complaint_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_cmp_complaint_duplicate_primary_complaint_id
    ON govos.cmp_complaint_duplicate (primary_complaint_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_cmp_complaint_duplicate_duplicate_complaint_id
    ON govos.cmp_complaint_duplicate (duplicate_complaint_id)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- cmp_complaint_merge (append-only metadata)
-- ---------------------------------------------------------------------------
CREATE TABLE govos.cmp_complaint_merge (
    id                      UUID            NOT NULL DEFAULT gen_random_uuid(),
    code                    VARCHAR(100),
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    surviving_complaint_id  UUID            NOT NULL,
    merged_complaint_id     UUID            NOT NULL,
    merged_by_user_id       UUID            NOT NULL,
    merge_reason            TEXT,
    merged_at               TIMESTAMPTZ     NOT NULL,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'COMPLETED',
    CONSTRAINT pk_cmp_complaint_merge PRIMARY KEY (id),
    CONSTRAINT fk_cmp_complaint_merge_surviving
        FOREIGN KEY (surviving_complaint_id) REFERENCES govos.cmp_complaint (id)
);

COMMENT ON TABLE govos.cmp_complaint_merge IS
    'Append-only merge record; merged_complaint_id is UUID only (cross-aggregate, no FK)';
COMMENT ON COLUMN govos.cmp_complaint_merge.surviving_complaint_id IS
    'Complaint that survives the merge (FK to cmp_complaint)';
COMMENT ON COLUMN govos.cmp_complaint_merge.merged_complaint_id IS
    'Complaint merged away (UUID only — no FK)';

CREATE INDEX idx_cmp_complaint_merge_surviving_complaint_id
    ON govos.cmp_complaint_merge (surviving_complaint_id);

CREATE INDEX idx_cmp_complaint_merge_merged_complaint_id
    ON govos.cmp_complaint_merge (merged_complaint_id);

CREATE INDEX idx_cmp_complaint_merge_merged_at
    ON govos.cmp_complaint_merge (merged_at);

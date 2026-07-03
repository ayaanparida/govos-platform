-- =============================================================================
-- GovOS Flyway Migration V1.6.0 — Workflow Engine (WRK)
-- =============================================================================
-- Domain    : WRK (Workflow)
-- ADR       : ADR-002 Modular Monolith, ADR-008 Flyway
-- =============================================================================

-- ---------------------------------------------------------------------------
-- wrk_workflow_definition
-- ---------------------------------------------------------------------------
CREATE TABLE govos.wrk_workflow_definition (
    id              UUID            NOT NULL,
    code            VARCHAR(100)    NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_by      VARCHAR(100)    NOT NULL,
    created_date    TIMESTAMPTZ     NOT NULL,
    updated_by      VARCHAR(100)    NOT NULL,
    updated_date    TIMESTAMPTZ     NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    description     VARCHAR(2000),
    CONSTRAINT pk_wrk_workflow_definition PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_wrk_workflow_definition_code_active
    ON govos.wrk_workflow_definition (code)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- wrk_workflow_version
-- ---------------------------------------------------------------------------
CREATE TABLE govos.wrk_workflow_version (
    id                      UUID            NOT NULL,
    code                    VARCHAR(100),
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    workflow_definition_id  UUID            NOT NULL,
    version_number          INTEGER         NOT NULL,
    published               BOOLEAN         NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_wrk_workflow_version PRIMARY KEY (id),
    CONSTRAINT fk_wrk_workflow_version_definition FOREIGN KEY (workflow_definition_id)
        REFERENCES govos.wrk_workflow_definition (id)
);

CREATE UNIQUE INDEX uk_wrk_workflow_version_def_number_active
    ON govos.wrk_workflow_version (workflow_definition_id, version_number)
    WHERE deleted = FALSE;

CREATE INDEX idx_wrk_workflow_version_definition_id
    ON govos.wrk_workflow_version (workflow_definition_id)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- wrk_workflow_step
-- ---------------------------------------------------------------------------
CREATE TABLE govos.wrk_workflow_step (
    id                  UUID            NOT NULL,
    code                VARCHAR(100),
    active              BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             BIGINT          NOT NULL DEFAULT 0,
    created_by          VARCHAR(100)    NOT NULL,
    created_date        TIMESTAMPTZ     NOT NULL,
    updated_by          VARCHAR(100)    NOT NULL,
    updated_date        TIMESTAMPTZ     NOT NULL,
    workflow_version_id UUID            NOT NULL,
    step_name           VARCHAR(255)    NOT NULL,
    step_type           VARCHAR(30)     NOT NULL,
    sequence_number     INTEGER         NOT NULL,
    sla_hours           INTEGER,
    CONSTRAINT pk_wrk_workflow_step PRIMARY KEY (id),
    CONSTRAINT fk_wrk_workflow_step_version FOREIGN KEY (workflow_version_id)
        REFERENCES govos.wrk_workflow_version (id)
);

CREATE UNIQUE INDEX uk_wrk_workflow_step_version_seq_active
    ON govos.wrk_workflow_step (workflow_version_id, sequence_number)
    WHERE deleted = FALSE;

CREATE INDEX idx_wrk_workflow_step_version_id
    ON govos.wrk_workflow_step (workflow_version_id)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- wrk_workflow_transition
-- ---------------------------------------------------------------------------
CREATE TABLE govos.wrk_workflow_transition (
    id                      UUID            NOT NULL,
    code                    VARCHAR(100),
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    from_step_id            UUID            NOT NULL,
    to_step_id              UUID            NOT NULL,
    condition_expression    VARCHAR(2000),
    CONSTRAINT pk_wrk_workflow_transition PRIMARY KEY (id),
    CONSTRAINT fk_wrk_workflow_transition_from FOREIGN KEY (from_step_id)
        REFERENCES govos.wrk_workflow_step (id),
    CONSTRAINT fk_wrk_workflow_transition_to FOREIGN KEY (to_step_id)
        REFERENCES govos.wrk_workflow_step (id)
);

CREATE UNIQUE INDEX uk_wrk_workflow_transition_from_to_active
    ON govos.wrk_workflow_transition (from_step_id, to_step_id)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- wrk_workflow_instance
-- ---------------------------------------------------------------------------
CREATE TABLE govos.wrk_workflow_instance (
    id                  UUID            NOT NULL,
    code                VARCHAR(100)    NOT NULL,
    active              BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             BIGINT          NOT NULL DEFAULT 0,
    created_by          VARCHAR(100)    NOT NULL,
    created_date        TIMESTAMPTZ     NOT NULL,
    updated_by          VARCHAR(100)    NOT NULL,
    updated_date        TIMESTAMPTZ     NOT NULL,
    workflow_version_id UUID            NOT NULL,
    reference_type      VARCHAR(100)    NOT NULL,
    reference_id        UUID            NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    started_at          TIMESTAMPTZ,
    completed_at        TIMESTAMPTZ,
    CONSTRAINT pk_wrk_workflow_instance PRIMARY KEY (id),
    CONSTRAINT fk_wrk_workflow_instance_version FOREIGN KEY (workflow_version_id)
        REFERENCES govos.wrk_workflow_version (id)
);

CREATE INDEX idx_wrk_workflow_instance_version_id
    ON govos.wrk_workflow_instance (workflow_version_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_wrk_workflow_instance_reference
    ON govos.wrk_workflow_instance (reference_type, reference_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_wrk_workflow_instance_status
    ON govos.wrk_workflow_instance (status)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- wrk_workflow_task
-- ---------------------------------------------------------------------------
CREATE TABLE govos.wrk_workflow_task (
    id                      UUID            NOT NULL,
    code                    VARCHAR(100),
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    workflow_instance_id    UUID            NOT NULL,
    assigned_to_id          UUID,
    assigned_role_id        UUID,
    step_id                 UUID            NOT NULL,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    due_date                TIMESTAMPTZ,
    completed_at            TIMESTAMPTZ,
    CONSTRAINT pk_wrk_workflow_task PRIMARY KEY (id),
    CONSTRAINT fk_wrk_workflow_task_instance FOREIGN KEY (workflow_instance_id)
        REFERENCES govos.wrk_workflow_instance (id),
    CONSTRAINT fk_wrk_workflow_task_assigned_to FOREIGN KEY (assigned_to_id)
        REFERENCES govos.idm_user (id),
    CONSTRAINT fk_wrk_workflow_task_assigned_role FOREIGN KEY (assigned_role_id)
        REFERENCES govos.idm_role (id),
    CONSTRAINT fk_wrk_workflow_task_step FOREIGN KEY (step_id)
        REFERENCES govos.wrk_workflow_step (id)
);

CREATE INDEX idx_wrk_workflow_task_instance_id
    ON govos.wrk_workflow_task (workflow_instance_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_wrk_workflow_task_assigned_to_id
    ON govos.wrk_workflow_task (assigned_to_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_wrk_workflow_task_status
    ON govos.wrk_workflow_task (status)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- wrk_workflow_history
-- ---------------------------------------------------------------------------
CREATE TABLE govos.wrk_workflow_history (
    id                      UUID            NOT NULL,
    code                    VARCHAR(100),
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    workflow_instance_id    UUID            NOT NULL,
    action                  VARCHAR(30)     NOT NULL,
    performed_by_id         UUID,
    performed_at            TIMESTAMPTZ     NOT NULL,
    remarks                 VARCHAR(2000),
    CONSTRAINT pk_wrk_workflow_history PRIMARY KEY (id),
    CONSTRAINT fk_wrk_workflow_history_instance FOREIGN KEY (workflow_instance_id)
        REFERENCES govos.wrk_workflow_instance (id),
    CONSTRAINT fk_wrk_workflow_history_performed_by FOREIGN KEY (performed_by_id)
        REFERENCES govos.idm_user (id)
);

CREATE INDEX idx_wrk_workflow_history_instance_id
    ON govos.wrk_workflow_history (workflow_instance_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_wrk_workflow_history_performed_at
    ON govos.wrk_workflow_history (performed_at DESC)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- wrk_workflow_assignment
-- ---------------------------------------------------------------------------
CREATE TABLE govos.wrk_workflow_assignment (
    id                  UUID            NOT NULL,
    code                VARCHAR(100),
    active              BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             BIGINT          NOT NULL DEFAULT 0,
    created_by          VARCHAR(100)    NOT NULL,
    created_date        TIMESTAMPTZ     NOT NULL,
    updated_by          VARCHAR(100)    NOT NULL,
    updated_date        TIMESTAMPTZ     NOT NULL,
    workflow_task_id    UUID            NOT NULL,
    user_id             UUID            NOT NULL,
    assigned_date       TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_wrk_workflow_assignment PRIMARY KEY (id),
    CONSTRAINT fk_wrk_workflow_assignment_task FOREIGN KEY (workflow_task_id)
        REFERENCES govos.wrk_workflow_task (id),
    CONSTRAINT fk_wrk_workflow_assignment_user FOREIGN KEY (user_id)
        REFERENCES govos.idm_user (id)
);

CREATE UNIQUE INDEX uk_wrk_workflow_assignment_task_user_active
    ON govos.wrk_workflow_assignment (workflow_task_id, user_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_wrk_workflow_assignment_user_id
    ON govos.wrk_workflow_assignment (user_id)
    WHERE deleted = FALSE;

COMMENT ON TABLE govos.wrk_workflow_definition IS
    'Reusable workflow blueprint — not bound to a specific business process (e.g. complaint)';
COMMENT ON COLUMN govos.wrk_workflow_instance.reference_type IS
    'Business entity type (e.g. COMPLAINT, LEAVE_APPLICATION) — decouples engine from domain';
COMMENT ON COLUMN govos.wrk_workflow_instance.reference_id IS
    'UUID of the business entity driving this workflow instance';

-- =============================================================================
-- GovOS Flyway Migration V1.7.0 — Audit (AUD)
-- =============================================================================
-- Domain    : AUD (Audit)
-- ADR       : ADR-002 Modular Monolith, ADR-008 Flyway
-- =============================================================================

-- ---------------------------------------------------------------------------
-- aud_session
-- ---------------------------------------------------------------------------
CREATE TABLE govos.aud_session (
    id              UUID            NOT NULL,
    code            VARCHAR(100),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_by      VARCHAR(100)    NOT NULL,
    created_date    TIMESTAMPTZ     NOT NULL,
    updated_by      VARCHAR(100)    NOT NULL,
    updated_date    TIMESTAMPTZ     NOT NULL,
    session_id      VARCHAR(255)    NOT NULL,
    login_time      TIMESTAMPTZ     NOT NULL,
    logout_time     TIMESTAMPTZ,
    ip_address      VARCHAR(45),
    device          VARCHAR(255),
    browser         VARCHAR(255),
    CONSTRAINT pk_aud_session PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_aud_session_session_id_active
    ON govos.aud_session (session_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_aud_session_login_time
    ON govos.aud_session (login_time DESC)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- aud_actor
-- ---------------------------------------------------------------------------
CREATE TABLE govos.aud_actor (
    id              UUID            NOT NULL,
    code            VARCHAR(100),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_by      VARCHAR(100)    NOT NULL,
    created_date    TIMESTAMPTZ     NOT NULL,
    updated_by      VARCHAR(100)    NOT NULL,
    updated_date    TIMESTAMPTZ     NOT NULL,
    user_id         UUID,
    display_name    VARCHAR(255)    NOT NULL,
    organization    VARCHAR(255),
    department      VARCHAR(255),
    CONSTRAINT pk_aud_actor PRIMARY KEY (id),
    CONSTRAINT fk_aud_actor_user FOREIGN KEY (user_id)
        REFERENCES govos.idm_user (id)
);

CREATE INDEX idx_aud_actor_user_id
    ON govos.aud_actor (user_id)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- aud_entity
-- ---------------------------------------------------------------------------
CREATE TABLE govos.aud_entity (
    id              UUID            NOT NULL,
    code            VARCHAR(100),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_by      VARCHAR(100)    NOT NULL,
    created_date    TIMESTAMPTZ     NOT NULL,
    updated_by      VARCHAR(100)    NOT NULL,
    updated_date    TIMESTAMPTZ     NOT NULL,
    entity_type     VARCHAR(100)    NOT NULL,
    entity_id       UUID            NOT NULL,
    entity_name     VARCHAR(500),
    CONSTRAINT pk_aud_entity PRIMARY KEY (id)
);

CREATE INDEX idx_aud_entity_type_id
    ON govos.aud_entity (entity_type, entity_id)
    WHERE deleted = FALSE;

CREATE UNIQUE INDEX uk_aud_entity_type_id_active
    ON govos.aud_entity (entity_type, entity_id)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- aud_event
-- ---------------------------------------------------------------------------
CREATE TABLE govos.aud_event (
    id              UUID            NOT NULL,
    code            VARCHAR(100),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_by      VARCHAR(100)    NOT NULL,
    created_date    TIMESTAMPTZ     NOT NULL,
    updated_by      VARCHAR(100)    NOT NULL,
    updated_date    TIMESTAMPTZ     NOT NULL,
    event_code      VARCHAR(100)    NOT NULL,
    event_type      VARCHAR(30)     NOT NULL,
    entity_type     VARCHAR(100)    NOT NULL,
    entity_id       UUID            NOT NULL,
    action          VARCHAR(30)     NOT NULL,
    description     VARCHAR(2000),
    actor_id        UUID,
    session_id      UUID,
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(500),
    event_timestamp TIMESTAMPTZ     NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'RECORDED',
    CONSTRAINT pk_aud_event PRIMARY KEY (id),
    CONSTRAINT fk_aud_event_actor FOREIGN KEY (actor_id)
        REFERENCES govos.aud_actor (id),
    CONSTRAINT fk_aud_event_session FOREIGN KEY (session_id)
        REFERENCES govos.aud_session (id)
);

CREATE UNIQUE INDEX uk_aud_event_event_code_active
    ON govos.aud_event (event_code)
    WHERE deleted = FALSE;

CREATE INDEX idx_aud_event_entity
    ON govos.aud_event (entity_type, entity_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_aud_event_timestamp
    ON govos.aud_event (event_timestamp DESC)
    WHERE deleted = FALSE;

CREATE INDEX idx_aud_event_actor_id
    ON govos.aud_event (actor_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_aud_event_session_id
    ON govos.aud_event (session_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_aud_event_status
    ON govos.aud_event (status)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- aud_change
-- ---------------------------------------------------------------------------
CREATE TABLE govos.aud_change (
    id              UUID            NOT NULL,
    code            VARCHAR(100),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_by      VARCHAR(100)    NOT NULL,
    created_date    TIMESTAMPTZ     NOT NULL,
    updated_by      VARCHAR(100)    NOT NULL,
    updated_date    TIMESTAMPTZ     NOT NULL,
    audit_event_id  UUID            NOT NULL,
    field_name      VARCHAR(255)    NOT NULL,
    old_value       TEXT,
    new_value       TEXT,
    CONSTRAINT pk_aud_change PRIMARY KEY (id),
    CONSTRAINT fk_aud_change_event FOREIGN KEY (audit_event_id)
        REFERENCES govos.aud_event (id)
);

CREATE INDEX idx_aud_change_event_id
    ON govos.aud_change (audit_event_id)
    WHERE deleted = FALSE;

CREATE UNIQUE INDEX uk_aud_change_event_field_active
    ON govos.aud_change (audit_event_id, field_name)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- aud_export
-- ---------------------------------------------------------------------------
CREATE TABLE govos.aud_export (
    id              UUID            NOT NULL,
    code            VARCHAR(100),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_by      VARCHAR(100)    NOT NULL,
    created_date    TIMESTAMPTZ     NOT NULL,
    updated_by      VARCHAR(100)    NOT NULL,
    updated_date    TIMESTAMPTZ     NOT NULL,
    export_type     VARCHAR(20)     NOT NULL,
    requested_by_id UUID            NOT NULL,
    requested_time  TIMESTAMPTZ     NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    file_name       VARCHAR(500),
    CONSTRAINT pk_aud_export PRIMARY KEY (id),
    CONSTRAINT fk_aud_export_requested_by FOREIGN KEY (requested_by_id)
        REFERENCES govos.idm_user (id)
);

CREATE INDEX idx_aud_export_requested_by_id
    ON govos.aud_export (requested_by_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_aud_export_status
    ON govos.aud_export (status)
    WHERE deleted = FALSE;

CREATE INDEX idx_aud_export_requested_time
    ON govos.aud_export (requested_time DESC)
    WHERE deleted = FALSE;

COMMENT ON TABLE govos.aud_event IS
    'Append-only audit event log — records who did what, when, and on which entity';
COMMENT ON COLUMN govos.aud_event.event_code IS
    'Unique business identifier for the audit event record';

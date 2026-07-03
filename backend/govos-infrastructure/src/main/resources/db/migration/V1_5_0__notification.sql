-- =============================================================================
-- GovOS Flyway Migration V1.5.0 — Notification (NTF)
-- =============================================================================
-- Domain    : NTF (Notification)
-- ADR       : ADR-002 Modular Monolith, ADR-008 Flyway
-- =============================================================================

-- ---------------------------------------------------------------------------
-- ntf_notification_channel
-- ---------------------------------------------------------------------------
CREATE TABLE govos.ntf_notification_channel (
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
    provider        VARCHAR(30)     NOT NULL,
    CONSTRAINT pk_ntf_notification_channel PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_ntf_notification_channel_code_active
    ON govos.ntf_notification_channel (code)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- ntf_notification_template
-- ---------------------------------------------------------------------------
CREATE TABLE govos.ntf_notification_template (
    id                  UUID            NOT NULL,
    code                VARCHAR(100)    NOT NULL,
    active              BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             BIGINT          NOT NULL DEFAULT 0,
    created_by          VARCHAR(100)    NOT NULL,
    created_date        TIMESTAMPTZ     NOT NULL,
    updated_by          VARCHAR(100)    NOT NULL,
    updated_date        TIMESTAMPTZ     NOT NULL,
    name                VARCHAR(255)    NOT NULL,
    channel_id          UUID            NOT NULL,
    subject_template    VARCHAR(500),
    body_template       TEXT,
    CONSTRAINT pk_ntf_notification_template PRIMARY KEY (id),
    CONSTRAINT fk_ntf_notification_template_channel FOREIGN KEY (channel_id) REFERENCES govos.ntf_notification_channel (id)
);

CREATE UNIQUE INDEX uk_ntf_notification_template_code_active
    ON govos.ntf_notification_template (code)
    WHERE deleted = FALSE;

CREATE INDEX idx_ntf_notification_template_channel_id
    ON govos.ntf_notification_template (channel_id)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- ntf_notification
-- ---------------------------------------------------------------------------
CREATE TABLE govos.ntf_notification (
    id              UUID            NOT NULL,
    code            VARCHAR(100)    NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_by      VARCHAR(100)    NOT NULL,
    created_date    TIMESTAMPTZ     NOT NULL,
    updated_by      VARCHAR(100)    NOT NULL,
    updated_date    TIMESTAMPTZ     NOT NULL,
    recipient       VARCHAR(500)    NOT NULL,
    subject         VARCHAR(500),
    body            TEXT,
    channel_id      UUID            NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    priority        VARCHAR(20)     NOT NULL DEFAULT 'NORMAL',
    scheduled_at    TIMESTAMPTZ,
    sent_at         TIMESTAMPTZ,
    CONSTRAINT pk_ntf_notification PRIMARY KEY (id),
    CONSTRAINT fk_ntf_notification_channel FOREIGN KEY (channel_id) REFERENCES govos.ntf_notification_channel (id)
);

CREATE UNIQUE INDEX uk_ntf_notification_code_active
    ON govos.ntf_notification (code)
    WHERE deleted = FALSE;

CREATE INDEX idx_ntf_notification_channel_id
    ON govos.ntf_notification (channel_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_ntf_notification_status
    ON govos.ntf_notification (status)
    WHERE deleted = FALSE;

CREATE INDEX idx_ntf_notification_scheduled_at
    ON govos.ntf_notification (scheduled_at)
    WHERE deleted = FALSE AND scheduled_at IS NOT NULL;

-- ---------------------------------------------------------------------------
-- ntf_notification_delivery
-- ---------------------------------------------------------------------------
CREATE TABLE govos.ntf_notification_delivery (
    id                  UUID            NOT NULL,
    code                VARCHAR(100),
    active              BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             BIGINT          NOT NULL DEFAULT 0,
    created_by          VARCHAR(100)    NOT NULL,
    created_date        TIMESTAMPTZ     NOT NULL,
    updated_by          VARCHAR(100)    NOT NULL,
    updated_date        TIMESTAMPTZ     NOT NULL,
    notification_id     UUID            NOT NULL,
    delivery_status     VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    provider_reference  VARCHAR(255),
    attempt_count       INTEGER         NOT NULL DEFAULT 0,
    last_attempt        TIMESTAMPTZ,
    CONSTRAINT pk_ntf_notification_delivery PRIMARY KEY (id),
    CONSTRAINT fk_ntf_notification_delivery_notification FOREIGN KEY (notification_id) REFERENCES govos.ntf_notification (id)
);

CREATE INDEX idx_ntf_notification_delivery_notification_id
    ON govos.ntf_notification_delivery (notification_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_ntf_notification_delivery_status
    ON govos.ntf_notification_delivery (delivery_status)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- ntf_notification_preference
-- ---------------------------------------------------------------------------
CREATE TABLE govos.ntf_notification_preference (
    id              UUID            NOT NULL,
    code            VARCHAR(100),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_by      VARCHAR(100)    NOT NULL,
    created_date    TIMESTAMPTZ     NOT NULL,
    updated_by      VARCHAR(100)    NOT NULL,
    updated_date    TIMESTAMPTZ     NOT NULL,
    user_id         UUID            NOT NULL,
    channel_id      UUID            NOT NULL,
    enabled         BOOLEAN         NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_ntf_notification_preference PRIMARY KEY (id),
    CONSTRAINT fk_ntf_notification_preference_user FOREIGN KEY (user_id) REFERENCES govos.idm_user (id),
    CONSTRAINT fk_ntf_notification_preference_channel FOREIGN KEY (channel_id) REFERENCES govos.ntf_notification_channel (id)
);

CREATE UNIQUE INDEX uk_ntf_notification_preference_user_channel_active
    ON govos.ntf_notification_preference (user_id, channel_id)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- ntf_notification_queue
-- ---------------------------------------------------------------------------
CREATE TABLE govos.ntf_notification_queue (
    id                  UUID            NOT NULL,
    code                VARCHAR(100),
    active              BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             BIGINT          NOT NULL DEFAULT 0,
    created_by          VARCHAR(100)    NOT NULL,
    created_date        TIMESTAMPTZ     NOT NULL,
    updated_by          VARCHAR(100)    NOT NULL,
    updated_date        TIMESTAMPTZ     NOT NULL,
    notification_id     UUID            NOT NULL,
    priority            VARCHAR(20)     NOT NULL DEFAULT 'NORMAL',
    next_execution      TIMESTAMPTZ,
    retry_count         INTEGER         NOT NULL DEFAULT 0,
    CONSTRAINT pk_ntf_notification_queue PRIMARY KEY (id),
    CONSTRAINT fk_ntf_notification_queue_notification FOREIGN KEY (notification_id) REFERENCES govos.ntf_notification (id)
);

CREATE INDEX idx_ntf_notification_queue_notification_id
    ON govos.ntf_notification_queue (notification_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_ntf_notification_queue_next_execution
    ON govos.ntf_notification_queue (next_execution)
    WHERE deleted = FALSE AND next_execution IS NOT NULL;

-- ---------------------------------------------------------------------------
-- ntf_notification_subscription
-- ---------------------------------------------------------------------------
CREATE TABLE govos.ntf_notification_subscription (
    id              UUID            NOT NULL,
    code            VARCHAR(100),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_by      VARCHAR(100)    NOT NULL,
    created_date    TIMESTAMPTZ     NOT NULL,
    updated_by      VARCHAR(100)    NOT NULL,
    updated_date    TIMESTAMPTZ     NOT NULL,
    user_id         UUID            NOT NULL,
    event_type      VARCHAR(100)    NOT NULL,
    channel_id      UUID            NOT NULL,
    CONSTRAINT pk_ntf_notification_subscription PRIMARY KEY (id),
    CONSTRAINT fk_ntf_notification_subscription_user FOREIGN KEY (user_id) REFERENCES govos.idm_user (id),
    CONSTRAINT fk_ntf_notification_subscription_channel FOREIGN KEY (channel_id) REFERENCES govos.ntf_notification_channel (id)
);

CREATE UNIQUE INDEX uk_ntf_notification_subscription_user_event_channel_active
    ON govos.ntf_notification_subscription (user_id, event_type, channel_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_ntf_notification_subscription_user_id
    ON govos.ntf_notification_subscription (user_id)
    WHERE deleted = FALSE;

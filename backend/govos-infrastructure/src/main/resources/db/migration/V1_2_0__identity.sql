-- =============================================================================
-- GovOS Flyway Migration V1.2.0 — Identity Management (IDM)
-- =============================================================================
-- Domain    : IDM (Identity Management)
-- ADR       : ADR-002 Modular Monolith, ADR-008 Flyway
-- =============================================================================

-- ---------------------------------------------------------------------------
-- idm_user
-- ---------------------------------------------------------------------------
CREATE TABLE govos.idm_user (
    id                      UUID            NOT NULL,
    code                    VARCHAR(100),
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    username                VARCHAR(100)    NOT NULL,
    email                   VARCHAR(255)    NOT NULL,
    mobile_number           VARCHAR(20),
    password_hash           VARCHAR(255)    NOT NULL,
    first_name              VARCHAR(100)    NOT NULL,
    middle_name             VARCHAR(100),
    last_name               VARCHAR(100)    NOT NULL,
    gender                  VARCHAR(20),
    date_of_birth           DATE,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    account_locked          BOOLEAN         NOT NULL DEFAULT FALSE,
    failed_login_attempts   INTEGER         NOT NULL DEFAULT 0,
    last_login              TIMESTAMPTZ,
    CONSTRAINT pk_idm_user PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_idm_user_username_active
    ON govos.idm_user (username)
    WHERE deleted = FALSE;

CREATE UNIQUE INDEX uk_idm_user_email_active
    ON govos.idm_user (email)
    WHERE deleted = FALSE;

CREATE INDEX idx_idm_user_status_active
    ON govos.idm_user (status)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- idm_role
-- ---------------------------------------------------------------------------
CREATE TABLE govos.idm_role (
    id              UUID            NOT NULL,
    code            VARCHAR(100)    NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_by      VARCHAR(100)    NOT NULL,
    created_date    TIMESTAMPTZ     NOT NULL,
    updated_by      VARCHAR(100)    NOT NULL,
    updated_date    TIMESTAMPTZ     NOT NULL,
    name            VARCHAR(150)    NOT NULL,
    description     VARCHAR(1000),
    system_role     BOOLEAN         NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_idm_role PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_idm_role_code_active
    ON govos.idm_role (code)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- idm_permission
-- ---------------------------------------------------------------------------
CREATE TABLE govos.idm_permission (
    id              UUID            NOT NULL,
    code            VARCHAR(100)    NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_by      VARCHAR(100)    NOT NULL,
    created_date    TIMESTAMPTZ     NOT NULL,
    updated_by      VARCHAR(100)    NOT NULL,
    updated_date    TIMESTAMPTZ     NOT NULL,
    module          VARCHAR(100)    NOT NULL,
    resource        VARCHAR(100)    NOT NULL,
    action          VARCHAR(50)     NOT NULL,
    description     VARCHAR(1000),
    CONSTRAINT pk_idm_permission PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_idm_permission_code_active
    ON govos.idm_permission (code)
    WHERE deleted = FALSE;

CREATE UNIQUE INDEX uk_idm_permission_module_resource_action_active
    ON govos.idm_permission (module, resource, action)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- idm_user_role
-- ---------------------------------------------------------------------------
CREATE TABLE govos.idm_user_role (
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
    role_id         UUID            NOT NULL,
    assigned_date   TIMESTAMPTZ     NOT NULL,
    expiry_date     TIMESTAMPTZ,
    CONSTRAINT pk_idm_user_role PRIMARY KEY (id),
    CONSTRAINT fk_idm_user_role_user FOREIGN KEY (user_id) REFERENCES govos.idm_user (id),
    CONSTRAINT fk_idm_user_role_role FOREIGN KEY (role_id) REFERENCES govos.idm_role (id)
);

CREATE UNIQUE INDEX uk_idm_user_role_user_role_active
    ON govos.idm_user_role (user_id, role_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_idm_user_role_user_id
    ON govos.idm_user_role (user_id)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- idm_role_permission
-- ---------------------------------------------------------------------------
CREATE TABLE govos.idm_role_permission (
    id              UUID            NOT NULL,
    code            VARCHAR(100),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_by      VARCHAR(100)    NOT NULL,
    created_date    TIMESTAMPTZ     NOT NULL,
    updated_by      VARCHAR(100)    NOT NULL,
    updated_date    TIMESTAMPTZ     NOT NULL,
    role_id         UUID            NOT NULL,
    permission_id   UUID            NOT NULL,
    CONSTRAINT pk_idm_role_permission PRIMARY KEY (id),
    CONSTRAINT fk_idm_role_permission_role FOREIGN KEY (role_id) REFERENCES govos.idm_role (id),
    CONSTRAINT fk_idm_role_permission_permission FOREIGN KEY (permission_id) REFERENCES govos.idm_permission (id)
);

CREATE UNIQUE INDEX uk_idm_role_permission_role_permission_active
    ON govos.idm_role_permission (role_id, permission_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_idm_role_permission_role_id
    ON govos.idm_role_permission (role_id)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- idm_refresh_token
-- ---------------------------------------------------------------------------
CREATE TABLE govos.idm_refresh_token (
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
    token           VARCHAR(500)    NOT NULL,
    expiry          TIMESTAMPTZ     NOT NULL,
    revoked         BOOLEAN         NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_idm_refresh_token PRIMARY KEY (id),
    CONSTRAINT fk_idm_refresh_token_user FOREIGN KEY (user_id) REFERENCES govos.idm_user (id)
);

CREATE UNIQUE INDEX uk_idm_refresh_token_token_active
    ON govos.idm_refresh_token (token)
    WHERE deleted = FALSE AND revoked = FALSE;

CREATE INDEX idx_idm_refresh_token_user_id
    ON govos.idm_refresh_token (user_id)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- idm_login_history
-- ---------------------------------------------------------------------------
CREATE TABLE govos.idm_login_history (
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
    login_time      TIMESTAMPTZ     NOT NULL,
    logout_time     TIMESTAMPTZ,
    ip_address      VARCHAR(45),
    device          VARCHAR(200),
    browser         VARCHAR(200),
    success         BOOLEAN         NOT NULL,
    CONSTRAINT pk_idm_login_history PRIMARY KEY (id),
    CONSTRAINT fk_idm_login_history_user FOREIGN KEY (user_id) REFERENCES govos.idm_user (id)
);

CREATE INDEX idx_idm_login_history_user_id
    ON govos.idm_login_history (user_id);

CREATE INDEX idx_idm_login_history_login_time
    ON govos.idm_login_history (login_time DESC);

-- ---------------------------------------------------------------------------
-- idm_password_history
-- ---------------------------------------------------------------------------
CREATE TABLE govos.idm_password_history (
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
    password_hash   VARCHAR(255)    NOT NULL,
    changed_date    TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_idm_password_history PRIMARY KEY (id),
    CONSTRAINT fk_idm_password_history_user FOREIGN KEY (user_id) REFERENCES govos.idm_user (id)
);

CREATE INDEX idx_idm_password_history_user_id
    ON govos.idm_password_history (user_id);

CREATE INDEX idx_idm_password_history_changed_date
    ON govos.idm_password_history (changed_date DESC);

COMMENT ON TABLE govos.idm_user IS 'Identity users — accounts for citizens, officers, and administrators';
COMMENT ON TABLE govos.idm_role IS 'Roles assignable to users';
COMMENT ON TABLE govos.idm_permission IS 'Fine-grained permissions (module:resource:action)';
COMMENT ON TABLE govos.idm_user_role IS 'User-to-role assignments';
COMMENT ON TABLE govos.idm_role_permission IS 'Role-to-permission grants';
COMMENT ON TABLE govos.idm_refresh_token IS 'Refresh token store (security integration deferred to govos-security)';
COMMENT ON TABLE govos.idm_login_history IS 'Login audit trail';
COMMENT ON TABLE govos.idm_password_history IS 'Password change history for reuse prevention';

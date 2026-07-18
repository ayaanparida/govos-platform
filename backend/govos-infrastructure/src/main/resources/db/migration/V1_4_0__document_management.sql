-- =============================================================================
-- GovOS Flyway Migration V1.4.0 — Document Management (DOC)
-- =============================================================================
-- Domain    : DOC (Document Management)
-- ADR       : ADR-002 Modular Monolith, ADR-008 Flyway
-- =============================================================================

-- ---------------------------------------------------------------------------
-- doc_storage_provider
-- ---------------------------------------------------------------------------
CREATE TABLE govos.doc_storage_provider (
    id              UUID            NOT NULL,
    code            VARCHAR(100)    NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_by      VARCHAR(100)    NOT NULL,
    created_date    TIMESTAMPTZ     NOT NULL,
    updated_by      VARCHAR(100)    NOT NULL,
    updated_date    TIMESTAMPTZ     NOT NULL,
    provider_type   VARCHAR(30)     NOT NULL,
    bucket_name     VARCHAR(255)    NOT NULL,
    endpoint        VARCHAR(500),
    CONSTRAINT pk_doc_storage_provider PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_doc_storage_provider_code_active
    ON govos.doc_storage_provider (code)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- doc_folder
-- ---------------------------------------------------------------------------
CREATE TABLE govos.doc_folder (
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
    parent_folder_id    UUID,
    owner_id            UUID            NOT NULL,
    CONSTRAINT pk_doc_folder PRIMARY KEY (id),
    CONSTRAINT fk_doc_folder_parent FOREIGN KEY (parent_folder_id) REFERENCES govos.doc_folder (id),
    CONSTRAINT fk_doc_folder_owner FOREIGN KEY (owner_id) REFERENCES govos.idm_user (id)
);

CREATE UNIQUE INDEX uk_doc_folder_code_active
    ON govos.doc_folder (code)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_folder_parent_id
    ON govos.doc_folder (parent_folder_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_folder_owner_id
    ON govos.doc_folder (owner_id)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- doc_document_tag
-- ---------------------------------------------------------------------------
CREATE TABLE govos.doc_document_tag (
    id              UUID            NOT NULL,
    code            VARCHAR(100),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_by      VARCHAR(100)    NOT NULL,
    created_date    TIMESTAMPTZ     NOT NULL,
    updated_by      VARCHAR(100)    NOT NULL,
    updated_date    TIMESTAMPTZ     NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    description     VARCHAR(500),
    CONSTRAINT pk_doc_document_tag PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_doc_document_tag_name_active
    ON govos.doc_document_tag (name)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- doc_document
-- ---------------------------------------------------------------------------
CREATE TABLE govos.doc_document (
    id                      UUID            NOT NULL,
    code                    VARCHAR(100)    NOT NULL,
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    original_filename       VARCHAR(500)    NOT NULL,
    stored_filename         VARCHAR(500)    NOT NULL,
    mime_type               VARCHAR(255),
    extension               VARCHAR(50),
    size_bytes              BIGINT,
    checksum                VARCHAR(128),
    storage_provider_id     UUID            NOT NULL,
    folder_id               UUID,
    owner_id                UUID            NOT NULL,
    visibility              VARCHAR(20)     NOT NULL DEFAULT 'PRIVATE',
    status                  VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
    CONSTRAINT pk_doc_document PRIMARY KEY (id),
    CONSTRAINT fk_doc_document_storage_provider FOREIGN KEY (storage_provider_id) REFERENCES govos.doc_storage_provider (id),
    CONSTRAINT fk_doc_document_folder FOREIGN KEY (folder_id) REFERENCES govos.doc_folder (id),
    CONSTRAINT fk_doc_document_owner FOREIGN KEY (owner_id) REFERENCES govos.idm_user (id)
);

CREATE UNIQUE INDEX uk_doc_document_code_active
    ON govos.doc_document (code)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_document_folder_id
    ON govos.doc_document (folder_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_document_owner_id
    ON govos.doc_document (owner_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_document_storage_provider_id
    ON govos.doc_document (storage_provider_id)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- doc_document_version
-- ---------------------------------------------------------------------------
CREATE TABLE govos.doc_document_version (
    id              UUID            NOT NULL,
    code            VARCHAR(100),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_by      VARCHAR(100)    NOT NULL,
    created_date    TIMESTAMPTZ     NOT NULL,
    updated_by      VARCHAR(100)    NOT NULL,
    updated_date    TIMESTAMPTZ     NOT NULL,
    document_id     UUID            NOT NULL,
    version_number  INTEGER         NOT NULL,
    checksum        VARCHAR(128),
    size_bytes      BIGINT,
    CONSTRAINT pk_doc_document_version PRIMARY KEY (id),
    CONSTRAINT fk_doc_document_version_document FOREIGN KEY (document_id) REFERENCES govos.doc_document (id)
);

CREATE UNIQUE INDEX uk_doc_document_version_doc_number_active
    ON govos.doc_document_version (document_id, version_number)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_document_version_document_id
    ON govos.doc_document_version (document_id)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- doc_document_tag_mapping
-- ---------------------------------------------------------------------------
CREATE TABLE govos.doc_document_tag_mapping (
    id              UUID            NOT NULL,
    code            VARCHAR(100),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_by      VARCHAR(100)    NOT NULL,
    created_date    TIMESTAMPTZ     NOT NULL,
    updated_by      VARCHAR(100)    NOT NULL,
    updated_date    TIMESTAMPTZ     NOT NULL,
    document_id     UUID            NOT NULL,
    tag_id          UUID            NOT NULL,
    CONSTRAINT pk_doc_document_tag_mapping PRIMARY KEY (id),
    CONSTRAINT fk_doc_document_tag_mapping_document FOREIGN KEY (document_id) REFERENCES govos.doc_document (id),
    CONSTRAINT fk_doc_document_tag_mapping_tag FOREIGN KEY (tag_id) REFERENCES govos.doc_document_tag (id)
);

CREATE UNIQUE INDEX uk_doc_document_tag_mapping_doc_tag_active
    ON govos.doc_document_tag_mapping (document_id, tag_id)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- doc_document_access_log
-- ---------------------------------------------------------------------------
CREATE TABLE govos.doc_document_access_log (
    id              UUID            NOT NULL,
    code            VARCHAR(100),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_by      VARCHAR(100)    NOT NULL,
    created_date    TIMESTAMPTZ     NOT NULL,
    updated_by      VARCHAR(100)    NOT NULL,
    updated_date    TIMESTAMPTZ     NOT NULL,
    document_id     UUID            NOT NULL,
    user_id         UUID            NOT NULL,
    action          VARCHAR(20)     NOT NULL,
    accessed_at     TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_doc_document_access_log PRIMARY KEY (id),
    CONSTRAINT fk_doc_document_access_log_document FOREIGN KEY (document_id) REFERENCES govos.doc_document (id),
    CONSTRAINT fk_doc_document_access_log_user FOREIGN KEY (user_id) REFERENCES govos.idm_user (id)
);

CREATE INDEX idx_doc_document_access_log_document_id
    ON govos.doc_document_access_log (document_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_document_access_log_user_id
    ON govos.doc_document_access_log (user_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_document_access_log_accessed_at
    ON govos.doc_document_access_log (accessed_at DESC)
    WHERE deleted = FALSE;

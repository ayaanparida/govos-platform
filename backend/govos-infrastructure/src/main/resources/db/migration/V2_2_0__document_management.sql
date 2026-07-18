-- =============================================================================
-- GovOS Flyway Migration V2.2.0 — Document Management (DOC)
-- =============================================================================
-- Version     : 2.2.0
-- Sprint      : DOC-003 Database Schema
-- Purpose     : Replace Sprint 0 DOC schema (V1_4_0) with GPS-001-compliant
--               DOC-002 domain model tables
-- Author      : GovOS Platform Team
-- Dependencies: V1__baseline.sql (schema govos, pgcrypto)
--               V1_4_0__document_management.sql (dropped in this migration)
--               V2_0_0__complaint.sql, V2_1_0__search.sql
-- ADR         : ADR-002 Modular Monolith, ADR-008 Flyway
-- Notes       : Cross-module references are UUID columns only (no FK to IDM/ORG)
--               FK constraints exist ONLY within DOC bounded context
--               No data migration, no seed data
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Drop legacy Sprint 0 DOC tables (V1_4_0)
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS govos.doc_document_access_log CASCADE;
DROP TABLE IF EXISTS govos.doc_document_tag_mapping CASCADE;
DROP TABLE IF EXISTS govos.doc_document_version CASCADE;
DROP TABLE IF EXISTS govos.doc_document CASCADE;
DROP TABLE IF EXISTS govos.doc_document_tag CASCADE;
DROP TABLE IF EXISTS govos.doc_folder CASCADE;
DROP TABLE IF EXISTS govos.doc_storage_provider CASCADE;

-- ---------------------------------------------------------------------------
-- doc_storage_provider (aggregate root)
-- ---------------------------------------------------------------------------
CREATE TABLE govos.doc_storage_provider (
    id                      UUID            NOT NULL DEFAULT gen_random_uuid(),
    code                    VARCHAR(100),
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    provider_name           VARCHAR(100)    NOT NULL,
    provider_type           VARCHAR(30)     NOT NULL,
    endpoint                VARCHAR(500),
    bucket_name             VARCHAR(255)    NOT NULL,
    region                  VARCHAR(100),
    encryption_enabled      BOOLEAN         NOT NULL DEFAULT TRUE,
    is_default              BOOLEAN         NOT NULL DEFAULT FALSE,
    secret_key_reference    VARCHAR(255),
    CONSTRAINT pk_doc_storage_provider PRIMARY KEY (id),
    CONSTRAINT ck_doc_storage_provider_type
        CHECK (provider_type IN (
            'MINIO', 'S3', 'AZURE_BLOB', 'GOOGLE_CLOUD_STORAGE', 'LOCAL'
        ))
);

COMMENT ON TABLE govos.doc_storage_provider IS
    'Storage backend configuration registry. Secrets stored via secret_key_reference only.';

CREATE UNIQUE INDEX uk_doc_storage_provider_name_active
    ON govos.doc_storage_provider (provider_name)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_storage_provider_type
    ON govos.doc_storage_provider (provider_type)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_storage_provider_active
    ON govos.doc_storage_provider (active)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- doc_document_retention_policy (aggregate root)
-- ---------------------------------------------------------------------------
CREATE TABLE govos.doc_document_retention_policy (
    id                      UUID            NOT NULL DEFAULT gen_random_uuid(),
    code                    VARCHAR(100),
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    organization_id         UUID,
    name                    VARCHAR(255)    NOT NULL,
    retention_days          INTEGER         NOT NULL DEFAULT 0,
    action_on_expiry        VARCHAR(30)     NOT NULL DEFAULT 'ARCHIVE',
    legal_hold              BOOLEAN         NOT NULL DEFAULT FALSE,
    description             VARCHAR(500),
    CONSTRAINT pk_doc_document_retention_policy PRIMARY KEY (id),
    CONSTRAINT ck_doc_retention_policy_days
        CHECK (retention_days >= 0),
    CONSTRAINT ck_doc_retention_policy_action
        CHECK (action_on_expiry IN ('DELETE', 'ARCHIVE', 'LEGAL_HOLD'))
);

COMMENT ON TABLE govos.doc_document_retention_policy IS
    'Retention and purge policy definitions. legal_hold blocks purge regardless of age.';
COMMENT ON COLUMN govos.doc_document_retention_policy.name IS
    'Policy display name (DOC-003 policy_name maps here).';

CREATE INDEX idx_doc_retention_policy_org_id
    ON govos.doc_document_retention_policy (organization_id)
    WHERE deleted = FALSE;

CREATE UNIQUE INDEX uk_doc_retention_policy_org_name_active
    ON govos.doc_document_retention_policy (organization_id, name)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_retention_policy_legal_hold
    ON govos.doc_document_retention_policy (legal_hold)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- doc_folder (aggregate root)
-- ---------------------------------------------------------------------------
CREATE TABLE govos.doc_folder (
    id                      UUID            NOT NULL DEFAULT gen_random_uuid(),
    code                    VARCHAR(100),
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    organization_id         UUID            NOT NULL,
    parent_folder_id        UUID,
    name                    VARCHAR(255)    NOT NULL,
    owner_id                UUID            NOT NULL,
    materialized_path       VARCHAR(2048),
    depth_level             INTEGER,
    CONSTRAINT pk_doc_folder PRIMARY KEY (id),
    CONSTRAINT fk_doc_folder_parent
        FOREIGN KEY (parent_folder_id) REFERENCES govos.doc_folder (id)
);

COMMENT ON TABLE govos.doc_folder IS
    'Hierarchical folder organization within an organization.';
COMMENT ON COLUMN govos.doc_folder.materialized_path IS
    'Materialized path for navigation (DOC-003 path index).';

CREATE INDEX idx_doc_folder_organization_id
    ON govos.doc_folder (organization_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_folder_parent_folder_id
    ON govos.doc_folder (parent_folder_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_folder_materialized_path
    ON govos.doc_folder (materialized_path)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_folder_created_date
    ON govos.doc_folder (created_date DESC)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- doc_document_category (aggregate root)
-- ---------------------------------------------------------------------------
CREATE TABLE govos.doc_document_category (
    id                          UUID            NOT NULL DEFAULT gen_random_uuid(),
    code                        VARCHAR(100)    NOT NULL,
    active                      BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                     BOOLEAN         NOT NULL DEFAULT FALSE,
    version                     BIGINT          NOT NULL DEFAULT 0,
    created_by                  VARCHAR(100)    NOT NULL,
    created_date                TIMESTAMPTZ     NOT NULL,
    updated_by                  VARCHAR(100)    NOT NULL,
    updated_date                TIMESTAMPTZ     NOT NULL,
    organization_id             UUID,
    parent_category_id          UUID,
    name                        VARCHAR(255)    NOT NULL,
    default_retention_policy_id UUID,
    allowed_mime_types          TEXT,
    description                 VARCHAR(500),
    CONSTRAINT pk_doc_document_category PRIMARY KEY (id),
    CONSTRAINT fk_doc_category_parent
        FOREIGN KEY (parent_category_id) REFERENCES govos.doc_document_category (id),
    CONSTRAINT fk_doc_category_default_retention
        FOREIGN KEY (default_retention_policy_id)
        REFERENCES govos.doc_document_retention_policy (id)
);

COMMENT ON TABLE govos.doc_document_category IS
    'Document taxonomy and classification tree.';

CREATE INDEX idx_doc_document_category_org_id
    ON govos.doc_document_category (organization_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_document_category_parent_id
    ON govos.doc_document_category (parent_category_id)
    WHERE deleted = FALSE;

CREATE UNIQUE INDEX uk_doc_document_category_org_code_active
    ON govos.doc_document_category (organization_id, code)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- doc_document (primary aggregate root)
-- ---------------------------------------------------------------------------
CREATE TABLE govos.doc_document (
    id                      UUID            NOT NULL DEFAULT gen_random_uuid(),
    code                    VARCHAR(100),
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    title                   VARCHAR(500)    NOT NULL,
    description             TEXT,
    organization_id         UUID            NOT NULL,
    owner_id                UUID            NOT NULL,
    status                  VARCHAR(30)     NOT NULL DEFAULT 'UPLOADED',
    classification          VARCHAR(30)     NOT NULL DEFAULT 'INTERNAL',
    mime_type               VARCHAR(255),
    module_code             VARCHAR(50),
    entity_type             VARCHAR(100),
    reference_id            UUID,
    folder_id               UUID,
    category_id             UUID,
    retention_policy_id     UUID,
    active_version_id       UUID,
    document_number         VARCHAR(100),
    tags                    TEXT,
    CONSTRAINT pk_doc_document PRIMARY KEY (id),
    CONSTRAINT fk_doc_document_folder
        FOREIGN KEY (folder_id) REFERENCES govos.doc_folder (id),
    CONSTRAINT fk_doc_document_category
        FOREIGN KEY (category_id) REFERENCES govos.doc_document_category (id),
    CONSTRAINT fk_doc_document_retention_policy
        FOREIGN KEY (retention_policy_id) REFERENCES govos.doc_document_retention_policy (id),
    CONSTRAINT ck_doc_document_status
        CHECK (status IN ('UPLOADED', 'PROCESSING', 'READY', 'ARCHIVED', 'DELETED')),
    CONSTRAINT ck_doc_document_classification
        CHECK (classification IN ('PUBLIC', 'INTERNAL', 'CONFIDENTIAL', 'RESTRICTED'))
);

COMMENT ON TABLE govos.doc_document IS
    'Document identity and lifecycle. Binary content stored in doc_document_version.';
COMMENT ON COLUMN govos.doc_document.owner_id IS
    'UUID reference to IDM user — no cross-context FK.';
COMMENT ON COLUMN govos.doc_document.active_version_id IS
    'Pointer to current version; FK added after doc_document_version creation.';

CREATE INDEX idx_doc_document_organization_id
    ON govos.doc_document (organization_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_document_status
    ON govos.doc_document (status)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_document_folder_id
    ON govos.doc_document (folder_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_document_category_id
    ON govos.doc_document (category_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_document_owner_id
    ON govos.doc_document (owner_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_document_active_version_id
    ON govos.doc_document (active_version_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_document_created_date
    ON govos.doc_document (created_date DESC)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_document_reference_id
    ON govos.doc_document (reference_id)
    WHERE deleted = FALSE;

CREATE UNIQUE INDEX uk_doc_document_org_document_number_active
    ON govos.doc_document (organization_id, document_number)
    WHERE deleted = FALSE AND document_number IS NOT NULL;

CREATE INDEX idx_doc_document_org_status
    ON govos.doc_document (organization_id, status)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_document_org_created_date
    ON govos.doc_document (organization_id, created_date DESC)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- doc_document_version (aggregate root)
-- ---------------------------------------------------------------------------
CREATE TABLE govos.doc_document_version (
    id                      UUID            NOT NULL DEFAULT gen_random_uuid(),
    code                    VARCHAR(100),
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    document_id             UUID            NOT NULL,
    version_number          INTEGER         NOT NULL,
    version_label           VARCHAR(20),
    checksum                VARCHAR(128)    NOT NULL,
    storage_provider_id     UUID            NOT NULL,
    storage_bucket          VARCHAR(255),
    storage_object_key      VARCHAR(1024)   NOT NULL,
    preview_storage_key     VARCHAR(1024),
    thumbnail_storage_key   VARCHAR(1024),
    mime_type               VARCHAR(255)    NOT NULL,
    original_filename       VARCHAR(500)    NOT NULL,
    extension               VARCHAR(50),
    size_bytes              BIGINT          NOT NULL,
    uploaded_by_id          UUID            NOT NULL,
    uploaded_at             TIMESTAMPTZ     NOT NULL,
    virus_scan_status       VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    ocr_status              VARCHAR(30)     NOT NULL DEFAULT 'NOT_STARTED',
    preview_status          VARCHAR(30)     NOT NULL DEFAULT 'NOT_GENERATED',
    version_status          VARCHAR(30)     NOT NULL DEFAULT 'ACTIVE',
    immutable               BOOLEAN         NOT NULL DEFAULT TRUE,
    embedding_version       INTEGER,
    CONSTRAINT pk_doc_document_version PRIMARY KEY (id),
    CONSTRAINT fk_doc_document_version_document
        FOREIGN KEY (document_id) REFERENCES govos.doc_document (id),
    CONSTRAINT fk_doc_document_version_storage_provider
        FOREIGN KEY (storage_provider_id) REFERENCES govos.doc_storage_provider (id),
    CONSTRAINT ck_doc_document_version_size
        CHECK (size_bytes > 0),
    CONSTRAINT ck_doc_document_version_number
        CHECK (version_number > 0),
    CONSTRAINT ck_doc_document_version_virus_scan
        CHECK (virus_scan_status IN ('PENDING', 'CLEAN', 'INFECTED', 'FAILED')),
    CONSTRAINT ck_doc_document_version_ocr
        CHECK (ocr_status IN ('NOT_STARTED', 'RUNNING', 'COMPLETED', 'FAILED')),
    CONSTRAINT ck_doc_document_version_preview
        CHECK (preview_status IN ('NOT_GENERATED', 'GENERATING', 'READY', 'FAILED')),
    CONSTRAINT ck_doc_document_version_status
        CHECK (version_status IN ('ACTIVE', 'ROLLED_BACK', 'SUPERSEDED'))
);

COMMENT ON TABLE govos.doc_document_version IS
    'Immutable document version blobs. Append-only after commit.';
COMMENT ON COLUMN govos.doc_document_version.original_filename IS
    'User filename at upload (DOC-003 file_name maps here).';
COMMENT ON COLUMN govos.doc_document_version.uploaded_by_id IS
    'UUID reference to IDM user — no cross-context FK.';
COMMENT ON COLUMN govos.doc_document_version.version_status IS
    'Version pointer status (DOC-003 status maps here).';

CREATE INDEX idx_doc_document_version_document_id
    ON govos.doc_document_version (document_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_document_version_checksum
    ON govos.doc_document_version (checksum)
    WHERE deleted = FALSE;

CREATE UNIQUE INDEX uk_doc_document_version_storage_object_key_active
    ON govos.doc_document_version (storage_object_key)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_document_version_uploaded_at
    ON govos.doc_document_version (uploaded_at DESC)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_document_version_version_status
    ON govos.doc_document_version (version_status)
    WHERE deleted = FALSE;

CREATE UNIQUE INDEX uk_doc_document_version_doc_number_active
    ON govos.doc_document_version (document_id, version_number)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_document_version_doc_version_number
    ON govos.doc_document_version (document_id, version_number)
    WHERE deleted = FALSE;

-- Deferred FK: document.active_version_id -> document_version
ALTER TABLE govos.doc_document
    ADD CONSTRAINT fk_doc_document_active_version
        FOREIGN KEY (active_version_id) REFERENCES govos.doc_document_version (id);

-- ---------------------------------------------------------------------------
-- doc_document_metadata (aggregate root)
-- ---------------------------------------------------------------------------
CREATE TABLE govos.doc_document_metadata (
    id                      UUID            NOT NULL DEFAULT gen_random_uuid(),
    code                    VARCHAR(100),
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    document_id             UUID            NOT NULL,
    document_version_id     UUID,
    ocr_text                TEXT,
    ocr_language            VARCHAR(20),
    ocr_confidence          DOUBLE PRECISION,
    extracted_metadata      TEXT,
    custom_attributes       TEXT,
    metadata_json           JSONB,
    page_count              INTEGER,
    language_detected       VARCHAR(20),
    watermark_applied       BOOLEAN         NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_doc_document_metadata PRIMARY KEY (id),
    CONSTRAINT fk_doc_document_metadata_document
        FOREIGN KEY (document_id) REFERENCES govos.doc_document (id),
    CONSTRAINT fk_doc_document_metadata_version
        FOREIGN KEY (document_version_id) REFERENCES govos.doc_document_version (id)
);

COMMENT ON TABLE govos.doc_document_metadata IS
    'Extended metadata and OCR extracted content for search indexing via SRH.';
COMMENT ON COLUMN govos.doc_document_metadata.metadata_json IS
    'Structured custom attributes (JSONB). custom_attributes retained for TEXT fallback.';

CREATE INDEX idx_doc_document_metadata_document_id
    ON govos.doc_document_metadata (document_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_document_metadata_version_id
    ON govos.doc_document_metadata (document_version_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_document_metadata_json_gin
    ON govos.doc_document_metadata USING gin (metadata_json)
    WHERE deleted = FALSE;

-- Full-text search placeholder for OCR text (DOC-017 SRH integration)
CREATE INDEX idx_doc_document_metadata_ocr_text_fts
    ON govos.doc_document_metadata
    USING gin (to_tsvector('english', coalesce(ocr_text, '')))
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- doc_document_share (aggregate root)
-- ---------------------------------------------------------------------------
CREATE TABLE govos.doc_document_share (
    id                      UUID            NOT NULL DEFAULT gen_random_uuid(),
    code                    VARCHAR(100),
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    document_id             UUID            NOT NULL,
    share_type              VARCHAR(30)     NOT NULL,
    shared_with_user_id     UUID,
    shared_with_role_id     UUID,
    shared_with_email       VARCHAR(255),
    created_by_id           UUID            NOT NULL,
    expires_at              TIMESTAMPTZ,
    permission              VARCHAR(30)     NOT NULL DEFAULT 'READ',
    token_hash              VARCHAR(256),
    signed_url_expires_at   TIMESTAMPTZ,
    public_link_url         VARCHAR(2048),
    CONSTRAINT pk_doc_document_share PRIMARY KEY (id),
    CONSTRAINT fk_doc_document_share_document
        FOREIGN KEY (document_id) REFERENCES govos.doc_document (id),
    CONSTRAINT ck_doc_document_share_type
        CHECK (share_type IN ('USER', 'ROLE', 'PUBLIC_LINK', 'SIGNED_URL'))
);

COMMENT ON TABLE govos.doc_document_share IS
    'Document sharing grants and time-limited links.';
COMMENT ON COLUMN govos.doc_document_share.shared_with_user_id IS
    'Recipient user UUID (DOC-003 recipient_user_id maps here).';
COMMENT ON COLUMN govos.doc_document_share.shared_with_role_id IS
    'Recipient role UUID (DOC-003 recipient_role maps here).';
COMMENT ON COLUMN govos.doc_document_share.token_hash IS
    'Hashed share token (DOC-003 share_token maps here).';
COMMENT ON COLUMN govos.doc_document_share.expires_at IS
    'Share expiry (DOC-003 expiry_date maps here).';

CREATE INDEX idx_doc_document_share_document_id
    ON govos.doc_document_share (document_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_document_share_recipient_id
    ON govos.doc_document_share (shared_with_user_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_document_share_expires_at
    ON govos.doc_document_share (expires_at)
    WHERE deleted = FALSE;

CREATE UNIQUE INDEX uk_doc_document_share_token_hash_active
    ON govos.doc_document_share (token_hash)
    WHERE deleted = FALSE AND token_hash IS NOT NULL;

-- ---------------------------------------------------------------------------
-- doc_document_access_log (aggregate root — append-only)
-- ---------------------------------------------------------------------------
CREATE TABLE govos.doc_document_access_log (
    id                      UUID            NOT NULL DEFAULT gen_random_uuid(),
    code                    VARCHAR(100),
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    document_id             UUID            NOT NULL,
    user_id                 UUID,
    operation               VARCHAR(30)     NOT NULL,
    accessed_at             TIMESTAMPTZ     NOT NULL,
    success                 BOOLEAN         NOT NULL DEFAULT TRUE,
    ip_address              VARCHAR(45),
    user_agent              VARCHAR(500),
    details                 VARCHAR(500),
    CONSTRAINT pk_doc_document_access_log PRIMARY KEY (id),
    CONSTRAINT fk_doc_document_access_log_document
        FOREIGN KEY (document_id) REFERENCES govos.doc_document (id),
    CONSTRAINT ck_doc_document_access_log_operation
        CHECK (operation IN (
            'UPLOAD', 'DOWNLOAD', 'PREVIEW', 'DELETE', 'SHARE', 'RESTORE', 'ROLLBACK'
        ))
);

COMMENT ON TABLE govos.doc_document_access_log IS
    'Append-only operational access audit. Platform compliance audit via AUD module.';
COMMENT ON COLUMN govos.doc_document_access_log.user_id IS
    'Actor UUID — no cross-context FK to IDM.';
COMMENT ON COLUMN govos.doc_document_access_log.accessed_at IS
    'Access timestamp (DOC-003 access_time maps here).';

CREATE INDEX idx_doc_access_log_document_id
    ON govos.doc_document_access_log (document_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_access_log_user_id
    ON govos.doc_document_access_log (user_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_access_log_operation
    ON govos.doc_document_access_log (operation)
    WHERE deleted = FALSE;

CREATE INDEX idx_doc_access_log_accessed_at
    ON govos.doc_document_access_log (accessed_at DESC)
    WHERE deleted = FALSE;

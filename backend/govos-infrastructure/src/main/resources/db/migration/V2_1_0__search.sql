-- =============================================================================
-- GovOS Flyway Migration V2.1.0 — Search (SRH)
-- =============================================================================
-- Version     : 2.1.0
-- Purpose     : Search platform domain schema
-- Author      : GovOS Platform Team
-- Architecture: SRH-001 (frozen), SRH-002.1 entities, SRH-003 migration
-- Dependencies: V1__baseline.sql (schema govos, pgcrypto)
--               V1_1_0 through V1_7_0 (platform foundation)
--               V2_0_0 (complaint)
-- ADR         : ADR-002 Modular Monolith, ADR-005 PostgreSQL, ADR-008 Flyway
-- Notes       : SearchDocumentMetadata embedded columns use md_ prefix
--               Cross-module references are UUID columns only (no FK to CMP/IDM/ORG)
--               No ON DELETE CASCADE — soft delete is application-managed
-- =============================================================================

-- ---------------------------------------------------------------------------
-- srh_search_index (aggregate root)
-- ---------------------------------------------------------------------------
CREATE TABLE govos.srh_search_index (
    id                      UUID            NOT NULL DEFAULT gen_random_uuid(),
    code                    VARCHAR(100),
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    name                    VARCHAR(255)    NOT NULL,
    description             VARCHAR(2000),
    engine_type             VARCHAR(30)     NOT NULL DEFAULT 'OPENSEARCH',
    status                  VARCHAR(30)     NOT NULL DEFAULT 'ACTIVE',
    mapping_version         INTEGER         NOT NULL DEFAULT 1,
    physical_index_name     VARCHAR(255),
    alias_name              VARCHAR(255),
    active_document_count   BIGINT          NOT NULL DEFAULT 0,
    last_reindexed_at       TIMESTAMPTZ,
    CONSTRAINT pk_srh_search_index PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_srh_search_index_code_active
    ON govos.srh_search_index (code)
    WHERE deleted = FALSE;

CREATE INDEX idx_srh_search_index_status
    ON govos.srh_search_index (status)
    WHERE deleted = FALSE;

CREATE INDEX idx_srh_search_index_engine_type
    ON govos.srh_search_index (engine_type)
    WHERE deleted = FALSE;

CREATE INDEX idx_srh_search_index_active
    ON govos.srh_search_index (active)
    WHERE deleted = FALSE;

CREATE INDEX idx_srh_search_index_deleted
    ON govos.srh_search_index (deleted);

COMMENT ON TABLE govos.srh_search_index IS
    'Aggregate root for logical search index configuration (SRH-001)';
COMMENT ON COLUMN govos.srh_search_index.code IS
    'Stable business index code, e.g. CMP_COMPLAINT, RTI_APPLICATION';

-- ---------------------------------------------------------------------------
-- srh_search_document (aggregate root)
-- ---------------------------------------------------------------------------
CREATE TABLE govos.srh_search_document (
    id                      UUID            NOT NULL DEFAULT gen_random_uuid(),
    code                    VARCHAR(100),
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    search_index_id         UUID            NOT NULL,
    search_document_id      UUID            NOT NULL,
    entity_type             VARCHAR(100)    NOT NULL,
    reference_id            UUID            NOT NULL,
    reference_code          VARCHAR(100),
    organization_id         UUID            NOT NULL,
    document_json           TEXT,
    search_text             TEXT,
    document_status         VARCHAR(30)     NOT NULL DEFAULT 'NOT_INDEXED',
    search_version          BIGINT          NOT NULL DEFAULT 0,
    indexed_at              TIMESTAMPTZ,
    last_indexed_at         TIMESTAMPTZ,
    -- Embedded SearchDocumentMetadata (md_ prefix)
    md_organization_id      UUID,
    md_entity_type          VARCHAR(100),
    md_reference_id         UUID,
    md_reference_code       VARCHAR(100),
    md_mapping_version      INTEGER,
    md_indexed_at           TIMESTAMPTZ,
    md_last_indexed_at      TIMESTAMPTZ,
    CONSTRAINT pk_srh_search_document PRIMARY KEY (id),
    CONSTRAINT fk_srh_search_document_index FOREIGN KEY (search_index_id)
        REFERENCES govos.srh_search_index (id)
);

CREATE UNIQUE INDEX uk_srh_search_document_index_entity_active
    ON govos.srh_search_document (search_index_id, entity_type, reference_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_srh_search_document_search_index_id
    ON govos.srh_search_document (search_index_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_srh_search_document_entity_type
    ON govos.srh_search_document (entity_type)
    WHERE deleted = FALSE;

CREATE INDEX idx_srh_search_document_reference_id
    ON govos.srh_search_document (reference_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_srh_search_document_organization_id
    ON govos.srh_search_document (organization_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_srh_search_document_document_status
    ON govos.srh_search_document (document_status)
    WHERE deleted = FALSE;

CREATE INDEX idx_srh_search_document_last_indexed_at
    ON govos.srh_search_document (last_indexed_at DESC)
    WHERE deleted = FALSE;

COMMENT ON TABLE govos.srh_search_document IS
    'Aggregate root for search document synchronization metadata (SRH-001)';

-- ---------------------------------------------------------------------------
-- srh_search_alias (aggregate root)
-- ---------------------------------------------------------------------------
CREATE TABLE govos.srh_search_alias (
    id                      UUID            NOT NULL DEFAULT gen_random_uuid(),
    code                    VARCHAR(100),
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    search_index_id         UUID            NOT NULL,
    alias_name              VARCHAR(255)    NOT NULL,
    physical_index_name     VARCHAR(255)    NOT NULL,
    active_alias            BOOLEAN         NOT NULL DEFAULT FALSE,
    switched_at             TIMESTAMPTZ,
    CONSTRAINT pk_srh_search_alias PRIMARY KEY (id),
    CONSTRAINT fk_srh_search_alias_index FOREIGN KEY (search_index_id)
        REFERENCES govos.srh_search_index (id)
);

CREATE UNIQUE INDEX uk_srh_search_alias_name_active
    ON govos.srh_search_alias (alias_name)
    WHERE deleted = FALSE;

CREATE INDEX idx_srh_search_alias_search_index_id
    ON govos.srh_search_alias (search_index_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_srh_search_alias_alias_name
    ON govos.srh_search_alias (alias_name)
    WHERE deleted = FALSE;

COMMENT ON TABLE govos.srh_search_alias IS
    'Aggregate root for search index alias routing (SRH-001)';

-- ---------------------------------------------------------------------------
-- srh_search_sync_job (aggregate root)
-- ---------------------------------------------------------------------------
CREATE TABLE govos.srh_search_sync_job (
    id                      UUID            NOT NULL DEFAULT gen_random_uuid(),
    code                    VARCHAR(100),
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    search_index_id         UUID            NOT NULL,
    job_name                VARCHAR(255)    NOT NULL,
    job_type                VARCHAR(30)     NOT NULL,
    job_status              VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    started_at              TIMESTAMPTZ,
    completed_at            TIMESTAMPTZ,
    processed_count         BIGINT          NOT NULL DEFAULT 0,
    success_count           BIGINT          NOT NULL DEFAULT 0,
    failure_count           BIGINT          NOT NULL DEFAULT 0,
    error_message           VARCHAR(2000),
    CONSTRAINT pk_srh_search_sync_job PRIMARY KEY (id),
    CONSTRAINT fk_srh_search_sync_job_index FOREIGN KEY (search_index_id)
        REFERENCES govos.srh_search_index (id),
    CONSTRAINT chk_srh_search_sync_job_processed_count
        CHECK (processed_count >= 0),
    CONSTRAINT chk_srh_search_sync_job_failure_count
        CHECK (failure_count >= 0)
);

CREATE INDEX idx_srh_search_sync_job_search_index_id
    ON govos.srh_search_sync_job (search_index_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_srh_search_sync_job_job_status
    ON govos.srh_search_sync_job (job_status)
    WHERE deleted = FALSE;

CREATE INDEX idx_srh_search_sync_job_job_type
    ON govos.srh_search_sync_job (job_type)
    WHERE deleted = FALSE;

COMMENT ON TABLE govos.srh_search_sync_job IS
    'Aggregate root for bulk search synchronization jobs (SRH-001)';

-- ---------------------------------------------------------------------------
-- srh_search_query_history (aggregate root — standalone)
-- ---------------------------------------------------------------------------
CREATE TABLE govos.srh_search_query_history (
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
    user_id                 UUID,
    query_text              VARCHAR(2000)   NOT NULL,
    query_type              VARCHAR(30)     NOT NULL DEFAULT 'SEARCH',
    filters_json            TEXT,
    result_count            BIGINT          NOT NULL DEFAULT 0,
    execution_time_ms       BIGINT          NOT NULL DEFAULT 0,
    searched_at             TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_srh_search_query_history PRIMARY KEY (id),
    CONSTRAINT chk_srh_search_query_history_execution_time_ms
        CHECK (execution_time_ms >= 0)
);

CREATE INDEX idx_srh_search_query_history_organization_id
    ON govos.srh_search_query_history (organization_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_srh_search_query_history_user_id
    ON govos.srh_search_query_history (user_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_srh_search_query_history_query_type
    ON govos.srh_search_query_history (query_type)
    WHERE deleted = FALSE;

CREATE INDEX idx_srh_search_query_history_created_date
    ON govos.srh_search_query_history (created_date DESC)
    WHERE deleted = FALSE;

COMMENT ON TABLE govos.srh_search_query_history IS
    'Aggregate root for search query history and analytics (SRH-001)';
